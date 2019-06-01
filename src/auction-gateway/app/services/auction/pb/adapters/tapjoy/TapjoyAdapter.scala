package services.auction.pb.adapters.tapjoy

import java.util.UUID

import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.openrtb.AuctionType.FirstPrice
import com.appodealx.openrtb.{BidRequest, BidResponse, Imp, Regs, Bid => RtbBid}
import io.circe.syntax._
import io.circe.{parser, Json}
import models.auction.NoBidReason._
import models.auction.{AdRequest, AdUnit, NoBidReason, _}
import monix.eval.Task
import play.api.Logger
import play.api.http.ContentTypes
import play.api.http.Status._
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import services.auction.pb.DefaultAdapter
import services.auction.pb.adapters.tapjoy.TapjoyBidExtension._
import services.settings.PbSettings

import cats.syntax.either._
import cats.syntax.option._

class TapjoyAdapter(ws: WSClient, pbSettings: PbSettings) extends DefaultAdapter[Task]("tapjoy") with CirceModelsInstances {

  private val logger = Logger(this.getClass)

  private val tMax        = pbSettings.pbTmax
  private val partnerName = ""

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], info: List[AdUnit]) = {

    val endpoint   = bidder.endpoint.toString
    val bidderName = bidder.title

    def doRequest(request: AdRequest[P], info: AdUnit) = {

      def nbr(response: BidResponse): Either[NoBidReason, BidResponse] =
        response.nbr.fold(response.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)

      def parseRes(response: WSResponse): Either[String, BidResponse] = {
        logger.debug(s"BidResponse: " + response.body)
        parser.parse(response.body).leftMap(_.message).flatMap(_.as[BidResponse].leftMap(_.message))
      }

      val placement = info.customParams.flatMap(_.as[TapjoyPlacement].toOption)
      val videoExt =
        if (request.reward) TapjoyVideoExtension(1, Some(0)) else TapjoyVideoExtension(0, Some(1))

      val placementId = placement.map(_.`placement_name`)

      def toBids(res: BidResponse) = {
        def rtbBidToBid(rtbBid: RtbBid) = Bid(
          price = rtbBid.price,
          adm = rtbBid.adm,
          impid = Some(rtbBid.impid),
          adid = rtbBid.adid,
          adomain = rtbBid.adomain,
          bundle = rtbBid.bundle,
          cat = rtbBid.cat,
          attr = rtbBid.attr,
          iurl = rtbBid.iurl,
          cid = rtbBid.cid,
          crid = rtbBid.crid,
          qagmediarating = rtbBid.qagmediarating,
          nurl = rtbBid.nurl,
          burl = rtbBid.burl,
          lurl = rtbBid.lurl,
          bidId = res.bidid,
          seatId = res.seatbid.flatMap(_.headOption.flatMap(_.seat)),
          adUnit = Some(info),
          placementId = placementId,
          dsp = Some(name),
          customResponse = rtbBid.ext.flatMap(_.as[TapjoyBidExtension].toOption).map(_.asJson),
          ext = rtbBid.ext
        )

        res.seatbid match {
          case Some(seat) => Right(seat.flatMap(_.bid).toList.map(rtbBidToBid))
          case None       => Left(NoBidReason.NoFill)
        }
      }

      val imp = {
        val i = Imp(
          id = UUID.randomUUID.toString,
          bidfloor = Some(request.bidFloor),
          bidfloorcur = Some("USD"),
          displaymanager = Some(partnerName),
          secure = Some(true)
        ).inject(request.ad)

        val mergedVideoExt = i.video.flatMap(_.ext).getOrElse(Json.obj()) deepMerge videoExt.asJson
        val videoObj       = i.video.map(_.copy(ext = mergedVideoExt.some))

        i.copy(video = videoObj)
      }

      val gdpr = if (request.gdpr.contains(true)) 1 else 0
      val regs = Regs(
        coppa = request.coppa,
        Some(Json.obj("gdpr" := gdpr))
      )

      val app = {
        val emptyJson    = Json.obj()
        val originalExt  = request.app.ext.getOrElse(emptyJson)
        val tapjoyAppExt = placement.map(_.asJson).getOrElse(emptyJson)

        request.app.copy(ext = (originalExt deepMerge tapjoyAppExt).some)
      }

      val bidRequest = BidRequest(
        id = request.id,
        imp = List(imp),
        app = Some(app),
        device = Some(request.device),
        user = Some(request.user),
        regs = Some(regs),
        at = Some(FirstPrice),
        test = request.test
      )

      logger.debug(s"BidRequest:\n${bidRequest.asJson.pretty(printer)}\n")

      val customContentType = ContentTypes.JSON + "; name=appodeal"

      Task.fromFuture {
        ws.url(endpoint)
          .withMethod("POST")
          .addHttpHeaders(HeaderNames.CONTENT_TYPE -> customContentType)
          .addHttpHeaders("x-openrtb-version" -> "2.3")
          .withBody(bidRequest.asJson.pretty(printer))
          .withRequestTimeout(tMax)
          .execute()
      }.map {
        case r if (r.status == NO_CONTENT || r.status == OK) && r.body.isEmpty =>
          logger.debug(s"NO FILL: `$bidderName` with status `${r.status}` and empty body for $endpoint")
          NoFill.asLeft
        case r if r.status == OK =>
          logger.debug(s"FILL: `$bidderName` with status `OK` for $endpoint")

          parseRes(r)
            .leftMap(ParsingError)
            .flatMap(nbr)
            .flatMap(toBids)

        case r =>
          logger.error(s"ERROR: `$bidderName` with status: ${r.status} for $endpoint")
          UnexpectedResponse(r.status).asLeft
      }.onErrorRecover {
        case e: Exception =>
          logger.error(e.getMessage, e)
          RequestException(e.getClass.getName).asLeft
      }
    }

    info.headOption match {
      case Some(i) => doRequest(request, i).map((false, _))
      case None    => Task.pure((false, Left(NoBidReason.NoFill)))
    }
  }
}
