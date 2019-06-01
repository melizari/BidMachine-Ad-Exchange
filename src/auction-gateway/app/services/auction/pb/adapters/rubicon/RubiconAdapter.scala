package services.auction.pb.adapters.rubicon

import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import com.appodealx.exchange.common.models.dto.{Banner, Native, Video}
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.exchange.common.models.{HtmlMarkup, XmlMarkup}
import com.appodealx.openrtb.native.response.{Native => NativeResponse}
import com.appodealx.openrtb.{BidRequest, Imp}
import io.circe._
import io.circe.syntax._
import models.PbAd
import models.auction.NoBidReason._
import models.auction.{AdRequest, AdUnit, AdapterResult, Bid, BiddingResult, NoBidReason}
import monix.eval.Task
import play.api.Logger
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import play.twirl.api.{Html, Xml}
import services.auction.pb.DefaultAdapter
import services.auction.pb.adapters.rubicon.RubiconAdapter._
import services.auction.pb.adapters.rubicon.model._
import services.auction.pb.adapters.rubicon.model.extensions._
import services.settings.rubicon.RubiconSettings

import cats.instances.option._
import cats.syntax.contravariantSemigroupal._
import cats.syntax.either._

import scala.concurrent.ExecutionContext

class RubiconAdapter(ws: WSClient, settings: RubiconSettings)(
  implicit ec: ExecutionContext
) extends DefaultAdapter[Task]("rubicon")
    with CirceRubiconInstances {

  private val logger        = Logger(this.getClass)
  private val customPrinter = Printer.noSpaces.copy(dropNullValues = true)
  private val rubiconRqEnc  = createRubiconBidRequestEncoder(rtbBidRequestEncoder)

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]): Task[AdapterResult] =
    for {
      (bidRequest, placementId) <- createRubiconBidRequest(request)
      bids                      <- getBids(request, bidRequest, bidder, placementId, adUnits.headOption)
    } yield (false, bids)

  override def prepareAd[A: Adm](bid: Bid): Task[PbAd] = {
    val markup = for {
      r <- bid.customResponse
      m <- r.as[String].toOption
    } yield m

    markup match {
      case Some(m) => doPrepare(m)
      case _       => Task.raiseError(new NoSuchElementException(s"$this: There is no markup in the Call"))
    }
  }

  private def doPrepare[A: Adm](markup: String) = {
    def prepareBanner =
      Task.now {
        PbAd(HtmlMarkup(Html(bannerHtmlScriptTagWith(markup))))
      }

    def prepareVideo =
      Task.now {
        PbAd(XmlMarkup(Xml(markup)))
      }

    def prepareNative =
      Task.raiseError[PbAd](new RuntimeException("not supported yet"))

    Adm[A] match {
      case adm if adm.is[Html]           => prepareBanner
      case adm if adm.is[VAST]           => prepareVideo
      case adm if adm.is[NativeResponse] => prepareNative
    }
  }

  private def createRubiconBidRequest[T: Plc](request: AdRequest[T]) = {

    import models.auction.PimpedAdRequest

    val bidRequest         = request.bidRequest
    val (imp, placementId) = getImpWithExtension(bidRequest)
    val app                = getAppWithExtension(bidRequest)

    Task.pure(bidRequest.copy(imp = imp, app = app) -> placementId)
  }

  private def getBids[T: Plc](request: AdRequest[T],
                              bidRequest: BidRequest,
                              bidder: Bidder,
                              placementId: Option[String],
                              adUnit: Option[AdUnit]) = {

    val bidRequestJson = if (Plc[T].is[Native]) bidRequest.asJson(rubiconRqEnc) else bidRequest.asJson

    logger.debug(s"BidRequest:\n${bidRequestJson.pretty(customPrinter)}\n")

    def message(r: WSResponse) =
      s"for bidder=`$name`, agency-id=`NO AGENCY`, status=${r.status}, emptyBody=${r.body.isEmpty}, endpoint=$bidder.endpoint.toString"

    Task.deferFuture {
      ws.url(bidder.endpoint.toString)
        .withMethod("POST")
        .addHttpHeaders(getHeaders(request): _*)
        .withBody(bidRequestJson.pretty(customPrinter))
        .withRequestTimeout(settings.timeout)
        .execute()
    }.map {
      case r if r.status == Status.OK && !r.body.isEmpty =>
        logger.debug("Fill " + message(r))
        responseToSoftBid(r, placementId, adUnit)
      case r if r.status == Status.OK =>
        logger.debug("NoFill " + message(r))
        NoFill.asLeft
      case r if r.status == Status.NO_CONTENT =>
        logger.debug("NoFill " + message(r))
        NoFill.asLeft
      case r =>
        logger.debug("UnexpectedResponse " + message(r))
        UnexpectedResponse(r.status).asLeft
    }.onErrorRecover {
      case e: Exception =>
        logger.error(e.getMessage, e)
        RequestException(e.getClass.getName).asLeft
    }
  }

  private def getHeaders(rq: AdRequest[_]) = {
    val auth      = settings.base64EncodedCredentials
    val userAgent = rq.device.ua.getOrElse("")

    List(
      HeaderNames.CONTENT_TYPE  -> ContentTypes.JSON,
      HeaderNames.AUTHORIZATION -> s"Basic $auth",
      HeaderNames.USER_AGENT    -> userAgent
    )
  }

  private def responseToSoftBid[T: Plc](wsr: WSResponse, placementId: Option[String], adUnit: Option[AdUnit]) =
    for {
      r0 <- toRubiconBidResponse(wsr)
      r1 <- checkResponseNbrField(r0)
      sb <- toBids(r1, placementId, adUnit)
    } yield sb

  private def toRubiconBidResponse(response: WSResponse) = {
    val json               = parser.parse(response.body).leftMap(_.message)
    val rubiconBidResponse = json.flatMap(_.as[RubiconBidResponse].leftMap(_.message))

    logger.debug(s"BidResponse: " + new String(response.bodyAsBytes.toArray))
    logger.debug(
      s"Parsing " +
        (if (rubiconBidResponse.isRight) "success"
         else s"failed with status: ${rubiconBidResponse.left.toOption.getOrElse("unknown")}")
    )

    rubiconBidResponse.leftMap(ParsingError)
  }

  private def checkResponseNbrField(r: RubiconBidResponse) =
    r.nbr.fold(r.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)



  private def toBids[T: Plc](rbs: RubiconBidResponse,
                             placementId: Option[String],
                             adUnit: Option[AdUnit]): BiddingResult = {

    def toBid(rb: RubiconBid) = {
      Bid(
        price = rb.price,
        adm = rb.adm,
        impid = Some(rb.impid),
        adomain = rb.adomain,
        crid = rb.crid,
        customResponse = if (Plc[T].is[Native]) rb.admobject else rb.adm.map(_.asJson),
        nurl = rb.nurl,
        dsp = Some(name),
        apiFramework = Some(Plc[T].apiFramework),
        placementId = placementId,
        adUnit = adUnit,
        ext = rb.ext
      )
    }


    rbs.seatbid.map { seatBids =>
      seatBids
        .flatMap(_.bid)
        .map(toBid)
        .filter(_.customResponse.isDefined)
    }
      .filter(_.nonEmpty)
      .toRight(NoBidReason.RequestException("no_proper_bids"))
  }

  private def createRubiconBidRequestEncoder(encoder: ObjectEncoder[BidRequest]): Encoder[BidRequest] = {

    def goDownToNativeField(bidRqJson: Json): ACursor = bidRqJson.hcursor.downField("imp").downArray.downField("native")

    def replaceNativeRqWithRubiconNativeRq(native: Json): Json = native.mapObject { n =>
      val requestField: Option[Json] = n("request")
      val rubiconNativeRq            = RubiconNativeRequest.Default

      requestField.fold(n)(_ => n.add("requestobj", rubiconNativeRq.asJson)).remove("request")
    }

    encoder.mapJson(bidJson => {
      goDownToNativeField(bidJson).withFocus(replaceNativeRqWithRubiconNativeRq).top.getOrElse(Json.Null)
    })
  }

  private def getImpWithExtension[T: Plc](bidRequest: BidRequest) = {
    val impExtension = ImpressionRubiconExtension(rp = ImpressionRubiconAdditions(`zone_id` = settings.zoneId))

    val (banner, bannerPlacementId) = getBannerWithExtension(bidRequest)
    val (video, videoPlacementId)   = getVideoWithExtension(bidRequest)

    val placementId = Plc[T] match {
      case p if p.is[Banner] => bannerPlacementId
      case p if p.is[Video]  => videoPlacementId
      case _                 => None
    }

    def impWithExtensions(imp: Imp) = List(
      imp.copy(
        ext = Some(impExtension.asJson),
        banner = banner,
        video = video
      )
    )

    bidRequest.imp.headOption.fold(List.empty[Imp])(impWithExtensions) -> placementId
  }

  private def getBannerWithExtension(bidRequest: BidRequest) = {
    val banner = bidRequest.imp.headOption.flatMap(imp => imp.banner)

    val widthAndHeight: Option[(Int, Int)] = banner.flatMap(b => (b.w, b.h).tupled)
    val size                               = widthAndHeight.map { case (w, h) => w + "x" + h }
    val sizeId                             = size.flatMap(settings.bannerSizeId)

    val bannerExtension = BannerRubiconExtension(rp = BannerRubiconAdditions(`size_id` = sizeId))

    banner.map(
      _.copy(
        mimes = Some(List("application/javascript")),
        ext = Some(bannerExtension.asJson)
      )
    ) -> sizeId.map(_.toString)
  }

  private def getVideoWithExtension(bidRequest: BidRequest) = {
    val video = bidRequest.imp.headOption.flatMap(imp => imp.video)

    val sizeId         = settings.videoSizeId
    val videoExtension = extensions.VideoRubiconExtension(rp = VideoRubiconAdditions(`size_id` = Some(sizeId)))

    video.map(_.copy(ext = Some(videoExtension.asJson))) -> Some(sizeId.toString)
  }

  private def getAppWithExtension(bidRequest: BidRequest) = {
    val publisherExtension = AppPublisherExtension(
      rp = AppPublisherAdditions(`account_id` = Some(settings.accountId))
    )

    val publisher = bidRequest.app
      .flatMap(_.publisher)
      .map(p => p.copy(ext = Some(publisherExtension.asJson)))

    val siteId       = settings.siteId
    val appExtension = AppRubiconExtension(rp = AppRubiconAdditions(`site_id` = Some(siteId)))

    bidRequest.app.map(_.copy(publisher = publisher, ext = Some(appExtension.asJson)))
  }
}

object RubiconAdapter {

  private def bannerHtmlScriptTagWith(markup: String) =
    s"""
       |<html>
       |  <head>
       |    <script type="text/javascript">$markup</script>
       |  </head>
       |  <body></body>
       |</html>
     """.stripMargin
}
