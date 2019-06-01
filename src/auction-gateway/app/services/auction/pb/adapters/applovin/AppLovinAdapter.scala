package services.auction.pb.adapters.applovin

import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.openrtb.ConnectionType
import io.circe.parser
import io.circe.syntax._
import models.auction.NoBidReason.{NoFill, ParsingError, RequestException, UnexpectedResponse}
import models.auction._
import monix.eval.Task
import play.api.http.Status
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Configuration, Logger}
import scalacache.CacheAlg
import services.auction.pb.DefaultAdapter
import services.settings.PbSettings

import cats.syntax.either._
import cats.syntax.option._

import scala.concurrent.duration._

class AppLovinAdapter(ws: WSClient,
                      pbSettings: PbSettings,
                      configuration: Configuration,
                      cache: CacheAlg[BiddingResult])
    extends DefaultAdapter[Task]("applovin")
    with CirceModelsInstances {

  import scalacache.Monix.modes._

  private val logger = Logger(this.getClass)

  private val tMax = pbSettings.pbTmax
  private val ttl  = Duration(configuration.get[String]("redis.applovin.ttl"))

  private def noZoneFailure(zoneId: String) =
    NoBidReason.RequestException(s"no_such_displaymanager_for_applovin_zone_${zoneId}_in_response")

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], info: List[AdUnit]) = {

    val displayManagerVersion = info.find(_.sdk.toLowerCase == name.toLowerCase).map(_.sdkVer)

    val infoByExtension =
      info.flatMap(i => i.customParams.flatMap(_.as[AppLovinExtensionObject].toOption).map(_ -> i)).toMap
    val infoByZone = infoByExtension.map { case (ext, dmi) => ext.`zone_id` -> dmi }
    val apiKey = infoByExtension
      .find(_._2.sdk == name)
      .map(_._1.`sdk_key`)
      .get

    def doRequest(name: String, zones: Map[String, AdUnit], sdkKey: String) = {
      val displayManagerName = name
      val zonesString        = zones.keys.mkString(",")

      logger.trace(s"DEBUG request: $request")
      logger.debug(s"DEBUG $displayManagerName:\n`zone=$zonesString`\n`sdk_key=$name`\n`tMax=$tMax`")
      logger.trace(s"rtb app ext: ${request.app.ext.map(_.toString()).getOrElse("EMPTY")}")
      logger.trace(s"rtb device ext: ${request.device.ext.map(_.toString()).getOrElse("EMPTY")}")
      logger.trace(s"rtb user ext: ${request.user.ext.map(_.toString()).getOrElse("EMPTY")}")
      logger.trace(s"INFOS: $info")

      val queryParams: List[(String, String)] = List(
        Some("sdk_key" -> sdkKey),
        request.app.ext.flatMap(_.hcursor.get[String]("packagename").toOption.map("package_name" -> _)),
        request.device.os.map("platform" -> _.toLowerCase),
        Some("format"                    -> "header_v1"),
        Some("zone_ids"                  -> zonesString),
        request.device.ifa.map("idfa"    -> _),
        request.device.dnt.map(b => "dnt" -> (if (b) 1.toString else 0.toString)).orElse(Some("dnt" -> 0.toString)),
        request.device.model.map("model"                              -> _),
        request.device.make.map("brand"                               -> _),
        request.device.osv.map("os"                                   -> _),
        request.device.ip.orElse(request.device.ipv6).map("device_ip" -> _),
        // Optional parameters
        // Locale
        getLocale(request).map("locale" -> _), // No language => no locale
        // User agent
        request.device.ua.map("ua" -> _),
        // Network
        rtbConnectionType2AppLovinNetwork(request).map("network" -> _),
        // session_uptime_ms
        //request.rtbApp.flatMap(_.ext.flatMap(a => a.hcursor.get[String]("session_uptime").toOption)).map("session_uptime_ms" -> _),
        // app_uptime_ms
        //request.rtbApp.flatMap(_.ext.flatMap(a => a.hcursor.get[String]("app_uptime").toOption)).map("app_uptime_ms" -> _),
        // session_depth
        request.app.ext
          .flatMap(
            _.hcursor.downField("imp").values.map(list => list.flatMap(j => j.asNumber.flatMap(jn => jn.toInt)).sum)
          )
          .map("session_depth"                                                                           -> _.toString),
        Some("ext_displaymanager"                                                                        -> name),
        displayManagerVersion.map("ext_displaymanager_ver"                                               -> _),
        Some("ext_bidfloor"                                                                              -> request.bidFloor.toString),
        Some("ext_bidfloorcur"                                                                           -> "USD"),
        request.app.ext.flatMap(_.hcursor.get[Long]("app_uptime").toOption).map("ext_app_uptime"         -> _.toString),
        request.app.ext.flatMap(_.hcursor.get[Long]("session_uptime").toOption).map("ext_session_uptime" -> _.toString),
        request.app.ext
          .flatMap(_.hcursor.get[Long]("session_id").toOption)
          .map("ext_appodeal_session_id" -> _.toString),
        request.app.ext
          .flatMap(_.hcursor.downField("imp").get[Long]("rewarded_video").toOption)
          .map("ext_appodeal_reward_imp" -> _.toString),
        request.app.ext
          .flatMap(_.hcursor.downField("imp").get[Long]("interstitial").toOption)
          .map("ext_appodeal_interstitial_imp" -> _.toString),
        request.app.ext
          .flatMap(_.hcursor.downField("imp").get[Long]("banner").toOption)
          .map("ext_appodeal_banner_imp" -> _.toString),
        request.app.ext
          .flatMap(_.hcursor.downField("click").get[Long]("rewarded_video").toOption)
          .map("ext_appodeal_reward_click" -> _.toString),
        request.app.ext
          .flatMap(_.hcursor.downField("click").get[Long]("interstitial").toOption)
          .map("ext_appodeal_interstitial_click" -> _.toString),
        request.app.ext
          .flatMap(_.hcursor.downField("click").get[Long]("banner").toOption)
          .map("ext_appodeal_banner_click"       -> _.toString),
        request.device.pxratio.map("ext_pxratio" -> _.toString),
        request.device.ppi.map("ext_ppi"         -> _.toString),
        request.app.ver.map("app_version"        -> _),
        request.device.carrier.map("carrier"     -> _),
        request.device.hwv.map("revision"        -> _),
        request.device.w.map("dx"                -> _.toString),
        request.device.h.map("dy"                -> _.toString)
      ).flatten

      val url = bidder.endpoint.toString

      Task.deferFuture {
        val req = ws
          .url(url)
          .withMethod("GET")
          .withQueryStringParameters(queryParams: _*)
          .withRequestTimeout(tMax)

        logger.debug(s"Request uri: ${req.uri.toString}")

        req.execute()
      }.map {
        case r if (r.status == Status.OK && r.body.isEmpty) || (r.status == Status.NO_CONTENT) =>
          logger.debug(
            s"NoFill for bidder: `$displayManagerName` and agency-id:`NO AGENCY` with status ${r.status} and body isEmpty=${r.body.isEmpty} for $queryParams and zones: $zonesString"
          )
          NoFill.asLeft
        case r if r.status == Status.OK =>
          logger.debug(
            s"Fill for bidder: `$displayManagerName` and agency-id:`NO AGENCY` with status ${r.status} for $queryParams and zones: $zonesString"
          )
          parseRes(r)
            .leftMap[NoBidReason](ParsingError)
            .flatMap { res =>
              val zoneId = res.`zone_id`
              (for {
                info  <- zones.find(t => t._1 == zoneId).map(_._2)
                price <- info.cpmEstimate
              } yield
                Bid(
                  price = price,
                  customResponse = res.asJson.some,
                  placementId = zoneId.some,
                  adUnit = infoByZone.get(zoneId),
                  dsp = Some(name)
                )).toRight(noZoneFailure(zoneId))
            }
        case r =>
          logger.debug(
            s"UnexpectedResponse for bidder: `$displayManagerName` and agency-id:`NO AGENCY` with status: ${r.status} for $queryParams and zones: ${zones.keys
              .mkString(",")}"
          )
          UnexpectedResponse(r.status).asLeft
      }.onErrorRecover {
        case e: Exception =>
          logger.error(e.getMessage, e)
          RequestException(e.getClass.getName).asLeft
      }
    }

    val requestTask = doRequest(name, infoByZone, apiKey).map(_.map(List(_)))

    def fetch(id: String) = {
      val key = cacheKey(Plc[P].name, id)
      cache
        .get(key)
        .flatMap {
          case Some(r) =>
            logger.debug(s"Getting bid from cache by key: $key.")
            Task.pure(r, true)
          case None =>
            logger.debug(s"Cache is empty for key: $key.")
            for {
              e <- requestTask
              _ <- cache.put(key)(e, ttl.some)
            } yield (e, false)
        }
    }

    val response = {
      request.impId.fold(requestTask.map((_, false)))(fetch)
    }

    response.map {
      // All zones are failed to load
      case (Left(reason), false) => false -> reason.asLeft
      // Adds only real requested zones
      case (Left(_), true)       => true   -> Nil.asRight
      case (Right(bids), cached) => cached -> bids.asRight
    }
  }

  private def rtbConnectionType2AppLovinNetwork[T](request: AdRequest[T]) =
    request.device.connectiontype match {
      case Some(c)
          if c == ConnectionType.Wifi ||
            c == ConnectionType.Ethernet ||
            c == ConnectionType.Unknown =>
        Some("wifi")
      case Some(c)
          if c == ConnectionType.Cellular2G ||
            c == ConnectionType.Cellular3G ||
            c == ConnectionType.Cellular4G ||
            c == ConnectionType.CellularUnknownGen =>
        Some("mobile")
      case _ => None
    }

  private def getLocale(request: AdRequest[_]): Option[String] = {
    import com.appodealx.exchange.common.utils.CountryParser
    for {
      l <- request.device.language
      c <- request.device.geo.flatMap(_.country.flatMap(CountryParser.parseAlpha2))
    } yield l + "_" + c
  }

  private def parseRes(response: WSResponse): Either[String, AppLovinResponse] = {
    logger.debug(s"Dirty response:\n" + response.body)
    parser.parse(response.body).leftMap(_.message).flatMap(_.as[AppLovinResponse].leftMap(_.message))
  }

  def cacheKey(adTypeName: String, id: String) = s"$name#v2#$adTypeName#$id"

}
