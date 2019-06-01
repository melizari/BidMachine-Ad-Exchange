package services.auction.pb.adapters.criteo.cdb

import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import com.appodealx.exchange.common.models.dto.Banner
import com.appodealx.exchange.common.models.{dto, HtmlMarkup, Size}
import io.circe.parser
import io.circe.syntax._
import models.PbAd
import models.auction.NoBidReason.{NoFill, ParsingError, QueriesLimitExceeded, RequestException}
import models.auction.{AdRequest, AdUnit, AdapterResult, Bid, BiddingResult, NoBidReason}
import monix.eval.Task
import play.api.Logger
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import play.twirl.api.Html
import redis.RedisClient
import scalacache.CacheAlg
import scalacache.Monix.modes._
import services.auction.pb.DefaultAdapter
import services.auction.pb.adapters.criteo.cdb.model._
import services.settings.PbSettings
import services.settings.criteo.CriteoCdbSettings

import cats.syntax.either._
import cats.syntax.option._

import scala.concurrent.ExecutionContext

class CriteoAdapter(ws: WSClient,
                    settings: CriteoCdbSettings,
                    pbSettings: PbSettings,
                    cache: CacheAlg[BiddingResult],
                    redisClient: RedisClient)(implicit ec: ExecutionContext)
    extends DefaultAdapter[Task]("criteo") {

  private val logger = Logger(this.getClass)

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) = {

    def announceBanner = {

      val adUnitsByIdx = adUnits.zipWithIndex.map(_.swap).toMap

      val Some(Size(w, h)) = Plc[P].size(request.ad)

      val zoneId: Option[Int] = {
        logger.debug(s"placement: ${Plc[P]}")
        logger.debug(s"isInterstitial: ${request.interstitial}")
        logger.debug(s"Size: $h x $w")

        if (Plc[P].is[Banner]) {
          settings.zones.get(w + "x" + h)
        } else {
          None
        }
      }

      def criteoResponseToBids(res: CriteoResponse): List[Bid] =
        res.slots
          .filter(s => zoneId.contains(s.zoneid) && s.width.contains(w) && s.height.contains(h))
          .map { s =>
            Bid(
              price = s.cpm,
              impid = Some(s.impid),
              customResponse = s.asJson.some,
              apiFramework = Plc[P].apiFramework.some,
              adUnit = adUnitsByIdx(s.impid.toInt).some,
              placementId = zoneId.map(_.toString),
              dsp = name.some,
            )
          }

      def responseToBiddingResult(response: WSResponse): BiddingResult =
        parser
          .parse(response.body)
          .leftMap(_.message)
          .flatMap(_.as[CriteoResponse].leftMap(_.message))
          .leftMap[NoBidReason](ParsingError)
          .map(criteoResponseToBids)

      def doRequest(zoneId: Int): Task[BiddingResult] = {
        import HeaderNames._

        val device = request.device

        def criteoRequest(zoneId: Int) = {

          val supportedOs = bidder.platforms.map(_.entryName).toSet
          val osOpt       = request.device.os.map(_.toLowerCase).filter(supportedOs)

          val bundleIdOpt =
            request.app.bundle
              .orElse(request.app.ext.flatMap(_.hcursor.get[String]("packagename").toOption))

          for {
            bundleId <- bundleIdOpt
            ifa      <- device.ifa
            os       <- osOpt
          } yield {

            val publisher = CriteoPublisher(bundleid = bundleId, publisherid = request.app.publisher.flatMap(_.id))

            val user = CriteoUser(
              deviceid = ifa,
              deviceidtype = if (os.contains("ios")) "IDFA" else "GAID",
              deviceos = os,
              lmt = device.lmt.map(if (_) "1" else "0")
            )

            val slots = adUnitsByIdx.map {
              case (idx, _) => SlotRequest(impid = idx.toString, zoneid = zoneId, native = Plc[P].is[dto.Native].some)
            }.toList

            CriteoRequest(publisher, user, slots)
          }
        }

        val headers =
          List(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON) ++
            device.ua.map(USER_AGENT    -> _) ++
            device.ip.map("X-Client-IP" -> _)

        def invoke(requestBody: String): Task[BiddingResult] =
          Task.deferFuture {
            val req = ws
              .url(bidder.endpoint.toString)
              .withMethod("POST")
              .addHttpHeaders(headers: _*)
              .withBody(requestBody)
              .withRequestTimeout(pbSettings.pbTmax)

            logger.debug(s"Request: $request")
            logger.debug(s"Request uri: $bidder.endpoint.toString")
            logger.debug(s"Criteo request body: $requestBody")
            logger.debug(s"Headers = ${req.headers}")

            req.execute()
          }.map {
            case r if r.status == Status.OK && r.body.nonEmpty =>
              logger.debug(s"Fill with body: ${r.body}")
              responseToBiddingResult(r)
            case r =>
              logger.debug(s"NoFill with status: ${r.status} and body: ${r.body}")
              NoFill.asLeft
          }.onErrorRecover {
            case e: Exception =>
              logger.error(e.getMessage, e)
              RequestException(e.getMessage).asLeft
          }

        criteoRequest(zoneId)
          .map(_.asJson.pretty(printer))
          .fold(
            Task.pure[BiddingResult](NoBidReason.RequestException("some params not found for criteo request").asLeft)
          )(body => invoke(body))
      }

      def noZoneRequestExceptionTask(zoneId: Option[Int]): Task[AdapterResult] =
        Task
          .now((false, NoBidReason.RequestException("zoneid for this ad type not found").asLeft[List[Bid]]))
          .memoize

      def fetch(id: String): Task[AdapterResult] = {
        val key = cacheKey(Plc[P].name, id)

        cache
          .get(key)
          .flatMap {
            case Some(r) =>
              logger.debug(s"Getting bid from cache by key: $key.")
              Task.pure((true, r))
            case None =>
              logger.debug(s"Cache is empty for key: $key.")

              zoneId match {
                case Some(z) =>
                  for {
                    biddingResult <- doRequest(z)
                    _             <- cache.put(key)(biddingResult, settings.ttl.some)
                  } yield (false, biddingResult)
                case None => noZoneRequestExceptionTask(zoneId)
              }
          }
      }

      val key = "criteo"

      request.impId match {
        case Some(id) =>
          for {
            c <- Task.deferFuture(redisClient.incr(key))
            _ <- if (c == 1) Task.deferFuture(redisClient.expire(key, 1)) else Task.now(false)
            r <- if (c > bidder.maxRpm) Task.pure((true, QueriesLimitExceeded.asLeft)) else fetch(id)
          } yield r

        case None => zoneId.fold(noZoneRequestExceptionTask(zoneId))(id => doRequest(id).map((false, _)))
      }
    }

    // Now supported only banner
    if (Plc[P].is[dto.Banner]) {
      announceBanner
    } else {
      val message = s"Type ${Plc[P].name} not supported yet"
      logger.error(message)
      Task.now((false, Nil.asRight[NoBidReason]))
    }
  }

  def cacheKey(adTypeName: String, id: String) = s"$name#v2#$adTypeName#$id"

  override def prepareAd[A: Adm](bid: Bid): Task[PbAd] =
    if (Adm[A].is[Html]) {
      val displayUrl = bid.customResponse.get
        .as[CriteoSlot]
        .right
        .get
        .displayurl
        .get
      val raw    = s"""<html><head><script src="$displayUrl"></script></head><body></body></html>"""
      Task.now(PbAd(HtmlMarkup(Html(raw))))
    } else {
      val message = s"This adm type not supported yet"
      logger.error(message)
      Task.raiseError(new RuntimeException(message))
    }
}
