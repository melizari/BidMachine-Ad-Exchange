package services.auction.pb.adapters.vungle

import java.util.UUID

import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.openrtb.AuctionType.FirstPrice
import com.appodealx.openrtb.{BidRequest, BidResponse, Imp, Regs}
import io.circe.syntax._
import io.circe.{parser, Printer}
import models.auction.{AdRequest, AdUnit, NoBidReason, _}
import monix.eval.Task
import play.api.Logger
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import services.auction.pb.DefaultAdapter
import services.settings.PbSettings

import cats.syntax.either._

class VungleAdapter(ws: WSClient, pbSettings: PbSettings)
    extends DefaultAdapter[Task]("vungle")
    with CirceModelsInstances {

  import NoBidReason._

  private val logger = Logger(this.getClass)

  private val customPrinter = Printer.noSpaces.copy(dropNullValues = true)

  private val tMax = pbSettings.pbTmax

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]): Task[AdapterResult] = {

    val vungleRtbEndpoint = bidder.endpoint.toString

    def doRequest(adUnit: AdUnit, request: AdRequest[P]): Task[BiddingResult] = {
      val extension = adUnit.customParams.flatMap(_.as[VungleExtensionObject].toOption)

      val vungleTokenOpt     = extension.map(_.token)
      val vunglePlacementOpt = extension.map(_.`placement_id`)

      val displayManagerName    = adUnit.sdk
      val displayManagerVersion = adUnit.sdkVer
      val rtbDevice             = request.device.copy(js = Some(true))

      val imp = Imp(
        id = UUID.randomUUID().toString,
        bidfloor = Some(request.bidFloor),
        bidfloorcur = Some("USD"),
        displaymanager = Some(displayManagerName),
        displaymanagerver = Some(displayManagerVersion),
        secure = Some(true),
        ext = vungleTokenOpt.map(t => VungleExt(BidToken(t)).asJson)
      ).inject(request.ad)

      val bidRequest = BidRequest(
        id = request.id,
        imp = List(imp),
        app = Some(request.app),
        device = Some(rtbDevice),
        user = Some(request.user),
        test = request.test,
        at = Some(FirstPrice),
        tmax = Some(tMax.toMillis.toInt),
        regs = Some(
          Regs(
            coppa = request.coppa,
            request.gdpr.map(b => if (b) 1 else 0).flatMap(b => parser.parse(s"""{"gdpr":$b}""").toOption)
          )
        )
      )

      Task.fromFuture {
        val body = bidRequest.asJson.pretty(customPrinter)

        logger.debug(s"BidRequest:$body")

        ws.url(vungleRtbEndpoint)
          .withMethod("POST")
          .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, "x-openrtb-version" -> "2.3")
          .withBody(body)
          .withRequestTimeout(tMax)
          .execute()

      }.map {
        case r if r.status == Status.OK && r.body.isEmpty =>
          logger.debug(
            s"NoFill for bidder: `$displayManagerName` and agency-id:`NO AGENCY` with status ${r.status} and body isEmpty=${r.body.isEmpty} for $vungleRtbEndpoint"
          )
          NoFill.asLeft
        case r if r.status == Status.NO_CONTENT =>
          logger.debug(
            s"NoFill for bidder: `$displayManagerName` and agency-id:`NO AGENCY` with status NoContent ${r.status} for $vungleRtbEndpoint"
          )
          NoFill.asLeft
        case r if r.status == Status.OK =>
          logger.debug(
            s"Fill for bidder: `$displayManagerName` and agency-id:`NO AGENCY` with status ${r.status} for $vungleRtbEndpoint"
          )
          parseRes(r)
            .leftMap(ParsingError)
            .flatMap(nbr)
            .flatMap(br => bidResponse2SoftBidEither[P](br, vunglePlacementOpt, adUnit))

        case r =>
          logger.debug(
            s"UnexpectedResponse for bidder: `$displayManagerName` and agency-id:`NO AGENCY` with status: ${r.status} for $vungleRtbEndpoint"
          )
          UnexpectedResponse(r.status).asLeft
      }.onErrorRecover {
        case e: Exception =>
          logger.error(e.getMessage, e)
          RequestException(e.getClass.getName).asLeft
      }
    }

    adUnits.headOption match {
      case Some(adUnit) => doRequest(adUnit, request).map((false, _))
      case None         => Task.pure((false, Nil.asRight[NoBidReason]))
    }
  }

  private def bidResponse2SoftBidEither[P: Plc](res: BidResponse,
                                                placementId: Option[String],
                                                adUnit: AdUnit): BiddingResult = {

    val noBidsError = Left(NoBidReason.RequestException("no bids in the response"))

    val bids = res.seatbid.map { seatBids =>
      for {
        seatbid <- seatBids
        rtbBid  <- seatbid.bid
      } yield
        Bid(
          price = rtbBid.price,
          adm = rtbBid.adm,
          impid = Some(rtbBid.impid),
          adid = rtbBid.adid,
          adomain = rtbBid.adomain,
          bundle = rtbBid.bundle,
          nurl = rtbBid.nurl,
          dsp = Some(name),
          cat = rtbBid.cat,
          attr = rtbBid.attr,
          iurl = rtbBid.iurl,
          qagmediarating = rtbBid.qagmediarating,
          cid = rtbBid.cid,
          crid = rtbBid.crid,
          bidId = res.bidid,
          seatId = seatbid.seat,
          placementId = placementId,
          adUnit = Some(adUnit),
          apiFramework = Some(adUnit.sdk),
          ext = rtbBid.ext
        )
    }

    bids
      .filter(_.nonEmpty)
      .fold[BiddingResult](noBidsError)(bs => Right(bs.toList))
  }

  private def parseRes(response: WSResponse): Either[String, BidResponse] = {
    logger.debug(s"Dirty response:\n" + new String(response.bodyAsBytes.toArray))
    val json     = parser.parse(response.body).leftMap(_.message)
    val r        = json.flatMap(_.as[BidResponse].leftMap(_.message))
    lazy val str = if (r.isRight) "success" else s"failed with status: ${r.left.toOption.getOrElse("unknown")}"
    logger.debug(s"Parsing " + str)
    r
  }

  private def nbr(response: BidResponse): Either[NoBidReason, BidResponse] = {
    logger.debug(s"BidResponse:" + response.asJson.pretty(printer))
    response.nbr.fold(response.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)
  }
}
