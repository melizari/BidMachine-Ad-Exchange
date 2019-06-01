package services.auction.pb.adapters.mytarget

import java.util.UUID

import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.common.models.dto
import com.appodealx.openrtb.AuctionType.FirstPrice
import com.appodealx.openrtb.{BidRequest, BidResponse, Imp, Native, Regs, Bid => RtbBid}
import io.circe.syntax._
import io.circe.{parser, Json, Printer}
import models.auction.NoBidReason._
import models.auction.{AdRequest, AdUnit, NoBidReason, _}
import monix.eval.Task
import monix.execution.Scheduler
import play.api.Logger
import play.api.http.Status._
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import services.auction.pb.DefaultAdapter
import services.settings.PbSettings

import cats.syntax.either._

class MyTargetAdapter(ws: WSClient, pbSettings: PbSettings, name: String)(implicit scheduler: Scheduler)
    extends DefaultAdapter[Task](name)
    with CirceModelsInstances {

  import io.circe.syntax.EncoderOps

  private val logger = Logger(this.getClass)
  private val p      = Printer.noSpaces.copy(dropNullValues = true)

  private val tMax = pbSettings.pbTmax
  private val defaultNativeRequest = Native(
    request = VideoNativeRequest.MyTargetDefault.asJson.pretty(p),
    ver = Some("1.0")
  )

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) = {

    def doRequest(request: AdRequest[P], adUnit: AdUnit) = {

      def nbr(response: BidResponse) =
        response.nbr.fold(response.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)

      def parseRes(response: WSResponse) = {
        logger.debug(s"BidResponse: " + new String(response.bodyAsBytes.toArray))
        val json     = parser.parse(response.body).leftMap(_.message)
        val r        = json.flatMap(_.as[BidResponse].leftMap(_.message))
        lazy val str = if (r.isRight) "success" else s"failed with status: ${r.left.toOption.getOrElse("unknown")}"
        logger.debug(s"Parsing " + str)
        r
      }

      def fromBidResponse(res: BidResponse, slotId: Long): Either[NoBidReason, List[Bid]] = {
        def fromBid(rtbBid: RtbBid): Bid =
          Bid(
            price = rtbBid.price,
            adm = rtbBid.adm,
            impid = Some(rtbBid.impid),
            adid = rtbBid.adid,
            adomain = rtbBid.adomain,
            placementId = Some(slotId.toString),
            cat = rtbBid.cat,
            iurl = rtbBid.iurl,
            cid = rtbBid.cid,
            crid = rtbBid.crid,
            qagmediarating = rtbBid.qagmediarating,
            attr = rtbBid.attr,
            bundle = rtbBid.bundle,
            bidId = res.bidid,
            seatId = res.seatbid.flatMap(_.headOption.flatMap(_.seat)), // Specification don't saying about seat, but we try to provide it.
            adUnit = Some(adUnit),
            apiFramework = Some(adUnit.sdk),
            customResponse = Some(Map("bid_id" -> rtbBid.id).asJson),
            dsp = Some(name),
            nurl = rtbBid.nurl,
            burl = rtbBid.burl,
            lurl = rtbBid.lurl,
            ext = rtbBid.ext
          )

        res.seatbid match {
          case Some(seats) => Right(seats.toList.flatMap(_.bid).map(fromBid))
          case None        => Left(NoBidReason.NoFill)
        }
      }

      val adSlot = adUnit.customParams
        .toRight(NoBidReason.RequestException("malformed_ad_unit"))
        .flatMap(_.as[MyTargetSlot].leftMap(e => NoBidReason.ParsingError(e.message)))

      def subImp(imp: Imp) =
        if (Plc[P].is[dto.Video]) {
          imp.copy(native = Some(defaultNativeRequest))
        } else {
          imp.inject(request.ad)
        }

      val imp = subImp(
        Imp(
          id = UUID.randomUUID.toString,
          bidfloor = Some(request.bidFloor),
          bidfloorcur = Some("USD"),
          secure = Some(true)
        )
      )

      val gdpr = if (request.gdpr.contains(true)) 1 else 0
      val regs = Regs(
        coppa = request.coppa,
        Some(Json.obj("gdpr" := gdpr))
      )

      val bidRequest = BidRequest(
        id = request.id,
        imp = imp :: Nil,
        app = Some(request.app),
        device = Some(request.device),
        user = Some(request.user),
        tmax = Some(tMax.toMillis.toInt),
        regs = Some(regs),
        at = Some(FirstPrice),
        cur = Some("USD" :: Nil)
      )

      val body = bidRequest.asJson.pretty(printer)

      def requestForSlot(adSlot: MyTargetSlot) = {
        val endpoint = s"${bidder.endpoint.toString}/${adSlot.`slot_id`}/?sdk=1"

        Task.deferFuture {

          logger.debug(s"Request URL: $endpoint")
          logger.debug(s"BidRequest: $body")

          ws.url(endpoint)
            .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
            .withRequestTimeout(tMax)
            .post(body)

        }.map {
          case res if (res.status == OK || res.status == NO_CONTENT) && res.body.isEmpty =>
            logger.debug(s"NO FILL: `${bidder.title}` with status ${res.status} for $endpoint")
            NoFill.asLeft
          case res if res.status == Status.OK =>
            logger.debug(s"FILL: `${bidder.title}` for $endpoint")
            parseRes(res)
              .leftMap(ParsingError)
              .flatMap(nbr)
              .flatMap(fromBidResponse(_, adSlot.`slot_id`))

          case res =>
            logger.debug(s"UNEXPECTED RESPONSE: `${adUnit.sdk}` with status: ${res.status} for $endpoint")
            UnexpectedResponse(res.status).asLeft
        }.onErrorRecover {
          case e: Exception =>
            logger.error(e.getMessage, e)
            RequestException(e.getClass.getName).asLeft
        }
      }

      adSlot.fold(e => Task.pure(Left(e)), requestForSlot)
    }

    adUnits.headOption match {
      case Some(adUnit) => doRequest(request, adUnit).map((false, _))
      case None         => Task.pure((false, Left(NoBidReason.NoFill)))
    }
  }
}
