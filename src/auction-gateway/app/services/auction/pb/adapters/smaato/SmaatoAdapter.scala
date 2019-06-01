package services.auction.pb.adapters.smaato

import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceRtbInstances
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.openrtb.native.response.Native
import io.circe.syntax._
import io.circe.{parser, Json}
import models.PbAd
import models.auction.NoBidReason.{NoFill, RequestException, UnexpectedResponse}
import models.auction.{AdRequest, AdUnit, Bid}
import monix.eval.Task
import play.api.Logger
import play.api.http.Status._
import play.api.libs.ws.WSClient
import play.twirl.api.{Html, Xml}
import services.auction.pb.DefaultAdapter
import services.auction.pb.adapters.smaato.model.nast.EventTrackerObject
import services.auction.pb.adapters.smaato.model.{SmaatoImage, SmaatoParams, SmaatoRichmedia}
import services.settings.PbSettings

import cats.syntax.either._

import scala.util.Try

object SmaatoAdapter {
  val SupportedOs = Set(Platform.iOS.prettyValue, Platform.Android.prettyValue)

  object Formats extends Enumeration {
    val display, video, native = Value
  }
}

class SmaatoAdapter(ws: WSClient, pbSettings: PbSettings)
    extends DefaultAdapter[Task]("smaato")
    with CirceRtbInstances {

  import SmaatoAdapter._

  private val logger = Logger(this.getClass)
  private val tMax   = pbSettings.pbTmax

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) = {

    val endpoint = bidder.endpoint.toString

    val format = Plc[P] match {
      case plc if plc.is[dto.Banner] => Formats.display
      case plc if plc.is[dto.Native] => Formats.native
      case plc if plc.is[dto.Video]  => Formats.video
    }

    val device = request.device
    val dnt    = device.lmt.getOrElse(false) || device.dnt.getOrElse(false)

    val gender = request.user.gender.flatMap(_.value match {
      case "M" => Some("m")
      case "F" => Some("f")
      case _   => None
    })

    val os        = request.device.os
    val isAndroid = os.contains(Platform.Android.prettyValue)
    val isIos     = os.contains(Platform.iOS.prettyValue)

    def doRequest(adUnit: AdUnit) = {
      val smaatoParams = adUnit.customParams.get.as[SmaatoParams].toOption.get

      val queryParams = List(
        device.ua.map("device" -> _),
        device.ip.map("devip"  -> _),
        if (format == Formats.video) Some("vastver"                         -> 3.toString) else None,
        if (format == Formats.video) smaatoParams.videotype.map("videotype" -> _.toString) else None,
        if (isIos) device.ifa.map("iosadid"                                 -> _) else None,
        if (isIos) Some("iosadtracking"                                     -> (!dnt).toString) else None,
        if (isAndroid) device.ifa.map("googleadid"                          -> _) else None,
        if (isAndroid) Some("googlednt"                                     -> dnt.toString) else None,
        Some("adspace"                         -> smaatoParams.adspace.toString),
        Some("pub"                             -> smaatoParams.pub.toString),
        Some("format"                          -> format.toString),
        request.coppa.map("coppa"              -> _.compare(false).toString),
        smaatoParams.dimension.map("dimension" -> _.toString),
        request.gdpr.map("gdpr"                -> _.compare(false).toString),
        request.consent.map("gdpr_consent"     -> _),
        device.carrier.map("carrier"           -> _),
        Some("mraidver"                        -> 2.toString)
        //        "ref" //expected in header
      ).flatten

      val targetingParams = List(
        request.user.keywords.map("kws" -> _),
        gender.map("gender"             -> _),
        request.user.geo
          .flatMap(geo => geo.lat.flatMap(lat => geo.lon.map(lon => "gps" -> s"$lat, $lon"))): Option[(String, String)],
        request.user.geo.flatMap(geo => geo.region.map("region"       -> _)),
        request.user.geo.flatMap(geo => geo.zip.map("zip"             -> _)),
        request.user.geo.flatMap(geo => geo.`type`.map("geotype"      -> _.value.toString)),
        request.user.geo.flatMap(geo => geo.ipservice.map("ipservice" -> _.value.toString)),
        request.device.model.map("devicemodel" -> _)
      ).flatten

      val params = queryParams ++ targetingParams

      Task.deferFuture {
        val req = ws
          .url(endpoint)
          .withMethod("GET")
          .withQueryStringParameters(params: _*)
          .withRequestTimeout(tMax)

        logger.debug(s"Request uri: ${req.uri.toString}")

        req.execute()
      }.map {
        case r if (r.status == OK || r.status == NO_CONTENT) && r.body.isEmpty =>
          logger.debug(s"NO FILL: `$name` with status ${r.status} for $params")
          NoFill.asLeft

        case r if r.status == OK =>
          logger.debug(s"FILL: `$name` with for $params")

          logger.debug(s"ParallelBidding.$this: Response: ${r.body}")

          Right(
            Bid(
              price = adUnit.cpmEstimate.getOrElse(0.0),
              adUnit = Some(adUnit),
              placementId = Some(smaatoParams.adspace.toString),
              apiFramework = Some(Plc[P].apiFramework),
              dsp = Some(name),
              customResponse =
                if (format != Formats.video) parser.parse(r.body).toOption
                else Some(Json.obj("vast" -> r.body.toString.asJson))
            ) :: Nil
          )

        case r =>
          logger.debug(s"UNEXPECTED: `$name` with status: ${r.status} for $params")
          UnexpectedResponse(r.status).asLeft
      }.onErrorRecover {
        case e: Exception =>
          logger.error(e.getMessage, e)
          RequestException(e.getClass.getName).asLeft
      }

    }

    Task.wanderUnordered(adUnits)(doRequest).map(flatResults).map((false, _))
  }

  override def prepareAd[A: Adm](bid: Bid): Task[PbAd] = {

    lazy val nse = Task.raiseError[PbAd](new NoSuchElementException(s"$this: No customResponse provided"))

    def banner = {

      def prepareImage(image: SmaatoImage) = {
        val smaatoImpressionTrackers = image.image.impressiontrackers
        val smaatoClickTrackers      = image.image.clicktrackers
        val url                      = image.image.img.url
        val ctaUrl                   = image.image.img.ctaurl
        val imageWidth               = image.image.img.w
        val imageHeight              = image.image.img.h
        val content =
          s"""<a href="$ctaUrl" target="_blank"><img width="$imageWidth" height="$imageHeight" style="border-style: none" src="$url" width="$imageWidth" height="$imageHeight"></a>"""
        val markup = HtmlMarkup(Html(content))

        PbAd(markup, smaatoImpressionTrackers, smaatoClickTrackers)
      }

      def prepareRichMedia(richMedia: SmaatoRichmedia) = {
        val smaatoImpressionTrackers = richMedia.richmedia.impressiontrackers
        val smaatoClickTrackers      = richMedia.richmedia.clicktrackers

        val raw    = richMedia.richmedia.mediadata.content
        val markup = HtmlMarkup(Html(raw))

        PbAd(markup, smaatoImpressionTrackers, smaatoClickTrackers)
      }

      bid.customResponse.fold(nse) { json =>
        val image     = json.as[SmaatoImage].map(prepareImage)
        val richmedia = json.as[SmaatoRichmedia].map(prepareRichMedia)

        Task.fromTry(image.orElse(richmedia).toTry)
      }
    }

    // Transform "eventtrackers" to "imptrackers" for NAST with ignoring js tag urls (method == 2).
    def transformNast2AppodealNast: Json => Option[Json] =
      (json: Json) =>
        Try(
          json.hcursor
            .downField("native")
            .withFocus(_.mapObject(it => it("eventtrackers").map(it.add("imptrackers", _).remove("eventtrackers")).get))
            .downField("imptrackers")
            .withFocus(
              j =>
                j.as[List[EventTrackerObject]]
                  .toOption
                  .map(_.filter(_.method == 1).map(_.url))
                  .getOrElse(Nil)
                  .asJson
            )
            .up
            .focus
        ).toOption.flatten

    def native = {
      val json = bid.customResponse.flatMap(transformNast2AppodealNast).flatMap(_.as[Native].toOption)
      json.fold(nse) { j =>
        Task.now(PbAd(NativeMarkup(j)))
      }
    }

    def video = {
      val xmlOpt = bid.customResponse
        .flatMap(_.as[Map[String, String]].toOption.flatMap(_.get("vast").map(Xml.apply)))

      xmlOpt.fold(nse) { raw =>
        Task.now(PbAd(XmlMarkup(raw)))
      }
    }

    Adm[A] match {
      case adm if adm.is[Html]   => banner
      case adm if adm.is[Native] => native
      case adm if adm.is[VAST]   => video
    }

  }

}
