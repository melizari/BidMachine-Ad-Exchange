package services.auction.pb.adapters.pubnative

import akka.util.ByteString
import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceRtbInstances
import com.appodealx.exchange.common.models.dto.Native
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.openrtb.Gender.{Female, Male}
import com.appodealx.openrtb.native.response.{Native => NativeReponse}
import io.circe.syntax._
import io.circe.{Encoder, parser}
import models.PbAd
import models.auction.NoBidReason.{RequestException, UnexpectedResponse}
import models.auction.{AdRequest, AdUnit, Bid, NoBidReason}
import monix.eval.Task
import play.api.Logger
import play.api.http.Status
import play.api.libs.ws.{WSClient, WSResponse}
import play.twirl.api.Html
import services.auction.pb.DefaultAdapter
import services.auction.pb.adapters.pubnative.PubNativeAdapter._
import services.auction.pb.adapters.pubnative.model._
import services.settings.PbSettings

import cats.instances.option._
import cats.syntax.either._
import cats.syntax.flatMap._

import scala.util.Try

object PubNativeAdapter {
  val iOS         = "ios"
  val Android     = "android"
  val SupportedOs = Set(iOS, Android)
}

class PubNativeAdapter(ws: WSClient, pbSettings: PbSettings)
    extends DefaultAdapter[Task]("pubnative")
    with CircePubNativeInstances
    with CirceRtbInstances {

  private val logger  = Logger(getClass)
  private val ios     = Platform.iOS.entryName
  private val android = Platform.Android.entryName
  private val tMax    = pbSettings.pbTmax

  implicit val byteStringEncoder: Encoder[ByteString] = Encoder.encodeString.contramap(_.utf8String)

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) = {

    val endpoint = bidder.endpoint.toString

    def parseRes(response: WSResponse) =
      parser
        .parse(response.body)
        .leftMap[NoBidReason](m => NoBidReason.ParsingError(m.message))
        .flatMap(_.as[PubNativeResponse].leftMap(m => NoBidReason.ParsingError(m.message)))

    def doRequest(adUnit: AdUnit) = {

      val params =
        adUnit.customParams
          .flatMap(_.as[PubNativeParams].toOption)
          .getOrElse(throw new NoSuchElementException(s"${this.toString}: no app_token or zone_id provided in request"))

      val placementId = params.`zone_id`.toString

      def toBids(response: PubNativeResponse) =
        response.ads.map { ad =>
          val price = ad.meta >>= (_.find(_.`type` == MetaType.Points)) >>= (_.data.text.map(_.toDouble / 1000))

          Bid(
            price = price.orElse(adUnit.cpmEstimate).getOrElse(0.0),
            apiFramework = Some(Plc[P].apiFramework),
            adUnit = Some(adUnit),
            customResponse = Some(response.asJson),
            placementId = Some(placementId),
            dsp = Some(name)
          )
        }.toList

      lazy val os = request.device.os.map(_.toLowerCase).filter(SupportedOs).get

      val al = {
        Plc[P].size(request.ad) match {
          case Some(Size(320, 50))    => "s"
          case Some(Size.Mrec)        => "m"
          case Some(Size(320, 480))   => "l"
          case _ if Plc[P].is[Native] => "m"
          case _                      => throw new NoSuchElementException(s"$this: unable get al size")
        }
      }

      val mf = "points,revenuemodel,campaignid,creativeid,contentinfo"

      val gender = request.user.gender.flatMap {
        case Male   => Some("m")
        case Female => Some("f")
        case _      => None
      }

      lazy val requiredQueryParams = Try(
        List(
          "apptoken"    -> params.`app_token`,
          "os"          -> os,
          "osver"       -> request.device.osv.get,
          "devicemodel" -> request.device.model.get,
          "dnt"         -> (if (request.device.dnt.getOrElse(true)) "1" else "0"),
          "al"          -> al,
          "mf"          -> mf,
          "srvi"        -> "1",
          "ua"          -> request.device.ua.get,
          "ip"          -> request.device.ip.get,
          "zoneid"      -> params.`zone_id`.toString
        )
      ).toOption match {
        case Some(s) => s
        case _       => throw new NoSuchElementException(s"${this.toString}: no required parameters provided")
      }

      lazy val additionalQueryParams = List(
        if (os == ios) request.device.ifa.map("idfa"    -> _) else None,
        if (os == android) request.device.ifa.map("gid" -> _) else None,
        request.user.id.map("uid" -> _),
        Some("adcount"            -> "1"),
        request.device.geo.flatMap(_.country.flatMap(s => CountryParser.parseAlpha2(s).map("locale" -> _))),
        request.device.geo.flatMap(_.lat.map("lat"  -> _.toString)),
        request.device.geo.flatMap(_.lon.map("long" -> _.toString)),
        gender.map("gender"                                -> _),
        request.coppa.map(if (_) "1" else "0").map("coppa" -> _),
        request.app.bundle.map("bundleid"                  -> _)
      ).flatten

      Task.fromFuture {
        val wsRequest = ws
          .url(endpoint)
          .withMethod("GET")
          .withQueryStringParameters(requiredQueryParams ++ additionalQueryParams: _*)
          .withRequestTimeout(tMax)

        logger.debug(s"request url: ${wsRequest.uri.toASCIIString}")

        wsRequest.execute()
      }.map {
        case r if r.status == Status.OK =>
          logger.debug(s"response:\n${r.body}")

          parseRes(r).map(toBids)

        case r => UnexpectedResponse(r.status).asLeft
      }.onErrorRecover {
        case e: Exception =>
          logger.error(e.getMessage, e)
          RequestException(e.getClass.getName).asLeft
      }
    }

    Task.wanderUnordered(adUnits)(doRequest).map(_.flatMap(_.toList).flatten).map {
      case Nil => (false, NoBidReason.NoFill.asLeft)
      case l   => (false, l.asRight[NoBidReason])
    }
  }

  override def prepareAd[A: Adm](bid: Bid) = {

    val response =
      bid.customResponse >>= (_.as[PubNativeResponse].toOption) match {
        case Some(pnr) => pnr
        case None      => throw new NoSuchElementException(s"$this: prepareAd: no PubNativeResponse provided")
      }

    def beaconsWithType(t: BeaconType) =
      response.ads.headOption
        .map(_.beacons.filter(_.`type` == t).flatMap(_.data.url))
        .getOrElse(Nil)

    val impTrackers     = beaconsWithType(BeaconType.Impression)
    val clickTrackers   = beaconsWithType(BeaconType.Click)

    Task {
      Adm[A] match {
        case adm if adm.is[Html] => PbAd(HtmlMarkup(Html(response.mraid)), impTrackers, clickTrackers)
        case adm if adm.is[NativeReponse] =>
          PbAd(NativeMarkup(response.nast))
        case adm if adm.is[VAST] => PbAd(XmlMarkup(response.vast), impTrackers, clickTrackers)
      }
    }
  }
}
