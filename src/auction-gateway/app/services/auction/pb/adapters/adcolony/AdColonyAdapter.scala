package services.auction.pb.adapters.adcolony

import java.util.UUID

import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.openrtb.AuctionType.FirstPrice
import com.appodealx.openrtb.{BidRequest, BidResponse, Imp, Regs, Bid => RtbBid}
import io.circe.Json
import io.circe.jawn.JawnParser
import io.circe.syntax._
import models.auction.NoBidReason._
import models.auction.{AdRequest, AdUnit, NoBidReason, _}
import monix.eval.Task
import play.api.Logger
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import services.auction.pb.DefaultAdapter
import services.settings.PbSettings

import cats.syntax.either._

class AdColonyAdapter(ws: WSClient, parser: JawnParser, pbSettings: PbSettings)
    extends DefaultAdapter[Task]("adcolony")
    with CirceModelsInstances {

  private val logger = Logger(this.getClass)

  private val tMax = pbSettings.pbTmax

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) = {

    val endpoint = bidder.endpoint.toString

    def doRequest(request: AdRequest[P], info: AdUnit) = {
      def zoneId(json: Json) = json.hcursor.downField("zone_id").as[String].toOption

      def nbr(response: BidResponse): Either[NoBidReason, BidResponse] = {
        logger.debug(s"BidResponse:" + response.asJson.pretty(printer))
        response.nbr.fold(response.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)
      }

      val placementId = info.customParams.flatMap(zoneId)

      def fromBidResponse(res: BidResponse): Either[NoBidReason, List[Bid]] = {
        def fromBid(rtbBid: RtbBid): Bid =
          Bid(
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
            placementId = placementId,
            bidId = res.bidid,
            seatId = res.seatbid.flatMap(_.headOption.flatMap(_.seat)),
            adUnit = Some(info),
            nurl = rtbBid.nurl,
            burl = rtbBid.burl,
            lurl = rtbBid.lurl,
            dsp = Some(name),
            ext = rtbBid.ext
          )

        res.seatbid match {
          case Some(seats) => Right(seats.toList.flatMap(_.bid).map(fromBid))
          case None        => Left(NoBidReason.NoFill)
        }
      }

      val bidderName = bidder.title
      val gdpr = if (request.gdpr.contains(true)) 1 else 0

      val imp = Imp(
        id = UUID.randomUUID.toString,
        bidfloor = Some(request.bidFloor),
        bidfloorcur = Some("USD"),
        displaymanager = Some(info.sdk),
        displaymanagerver = Some(info.sdkVer),
        secure = Some(true),
        ext = info.customParams
      ).inject(request.ad)

      val regs = Regs(
        coppa = request.coppa,
        Some(Json.obj("gdpr" := gdpr))
      )

      val bidRequest = BidRequest(
        id = request.id,
        imp = List(imp),
        app = Some(request.app),
        device = Some(request.device.copy(js = Some(true))),
        user = Some(request.user),
        test = request.test,
        at = Some(FirstPrice),
        tmax = Some(tMax.toMillis.toInt),
        regs = Some(regs)
      )

      logger.debug(s"BidRequest:\n${bidRequest.asJson.pretty(printer)}\n")

      Task.fromFuture {
        ws.url(endpoint)
          .withMethod("POST")
          .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
          .addHttpHeaders("x-openrtb-version" -> "2.3")
          .withBody(bidRequest.asJson.pretty(printer))
          .withRequestTimeout(tMax)
          .execute()
      }.map {
        case r if r.status == Status.OK && r.body.isEmpty =>
          logger.debug(
            s"NoFill for bidder: `$bidderName` and agency-id:`NO AGENCY` with status ${r.status} and body isEmpty=${r.body.isEmpty} for $endpoint"
          )
          NoFill.asLeft
        case r if r.status == Status.NO_CONTENT =>
          logger.debug(
            s"NoFill for bidder: `$bidderName` and agency-id:`NO AGENCY` with status NoContent ${r.status} for $endpoint"
          )
          NoFill.asLeft
        case r if r.status == Status.OK =>
          logger.debug(
            s"Fill for bidder: `$bidderName` and agency-id:`NO AGENCY` with status ${r.status} for $endpoint"
          )

          parseRes(r).leftMap(ParsingError).flatMap(nbr).flatMap(br => fromBidResponse(br))
        case r =>
          logger.debug(
            s"UnexpectedResponse for bidder: `$bidderName` and agency-id:`NO AGENCY` with status: ${r.status} for $endpoint"
          )

          UnexpectedResponse(r.status).asLeft
      }.onErrorRecover {
        case e: Exception =>
          logger.error(e.getMessage, e)

          RequestException(e.getClass.getName).asLeft
      }
    }

    adUnits.headOption match {
      case Some(i) => doRequest(request, i).map((false, _))
      case None    => Task.pure((false, Left(NoBidReason.NoFill)))
    }
  }

  def parseRes(response: WSResponse): Either[String, BidResponse] = {
    logger.debug(s"Dirty response:\n" + response.body)

    val res = {
      for {
        j <- parser.parseByteBuffer(response.bodyAsBytes.asByteBuffer).leftMap(_.message)
        r <- j.as[BidResponse].leftMap(_.message)
      } yield r
    }

    logger.debug(s"Parsing " + res.fold(status => s"failed with status: $status", _ => "success"))
    res
  }

}
