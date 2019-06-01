package services.auction.pb.adapters.openx

import java.util.UUID

import com.appodealx.exchange.common.models.auction.Macros._
import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.common.models.{Failure, FailureReason}
import com.appodealx.exchange.common.services.SubstitutionService
import com.appodealx.openrtb.AuctionType.FirstPrice
import com.appodealx.openrtb._
import io.circe.JsonObject
import io.circe.jawn.JawnParser
import io.circe.syntax._
import models.PbAd
import models.auction.{AdRequest, AdUnit, Bid, NoBidReason, _}
import models.auction.NoBidReason._
import monix.eval.Task
import play.api.Logger
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import services.auction.pb.DefaultAdapter
import services.settings.PbSettings

import cats.syntax.either._

class OpenXAdapter(ws: WSClient,
                   parser: JawnParser,
                   pbSettings: PbSettings,
                   settings: OpenXConfiguration,
                   substitutionService: SubstitutionService)
    extends DefaultAdapter[Task]("openx")
    with CirceModelsInstances {

  private val logger = Logger(this.getClass)

  private val tMax = pbSettings.pbTmax

  private def substitute(params: Map[String, String])(s: String) = substitutionService.substitute(params)(s)

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) = {

    val endpoint = bidder.endpoint.toString

    def doRequest(adUnit: AdUnit, request: AdRequest[P]): Task[BiddingResult] = {

      val rtbDevice = request.device.copy(js = Some(true))

      val imp = Imp(
        id = UUID.randomUUID().toString,
        bidfloor = Some(request.bidFloor),
        bidfloorcur = Some("USD"),
        secure = Some(true),
        ext = adUnit.customParams
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
        cur = Some(List("USD")),
        source = None,
        regs = Some(
          Regs(
            coppa = request.coppa,
            ext = request.gdpr.map(b => ("gdpr", (if (b) 1 else 0).asJson)).map(JsonObject(_).asJson)
          )
        )
      )

      logger.debug(s"BidRequest:\n${bidRequest.asJson.pretty(printer)}\n")

      def message(actionName: String, r: WSResponse) =
        s"$actionName for bidder=`$name`, status=${r.status}, emptyBody=${r.body.isEmpty}, endpoint=$endpoint"

      Task.fromFuture {
        ws.url(endpoint)
          .withMethod("POST")
          .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON, "x-openrtb-version" -> "2.5")
          .withBody(bidRequest.asJson.pretty(printer))
          .withRequestTimeout(tMax)
          .execute()
      }.map {
        case r if r.status == Status.OK && r.body.isEmpty =>
          logger.debug(message("NoFill", r))
          NoFill.asLeft
        case r if r.status == Status.NO_CONTENT =>
          logger.debug(message("NoFill", r))
          NoFill.asLeft
        case r if r.status == Status.OK =>
          logger.debug(message("Fill", r))
          parseRes(r).leftMap(ParsingError).flatMap(nbr).flatMap(br => bidResponseToBids[P](br, adUnit))
        case r =>
          logger.debug(message("UnexpectedResponse", r))
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

  def bidResponseToBids[P: Plc](res: BidResponse, adUnit: AdUnit): BiddingResult = {

    val noBidsError = Left(NoBidReason.RequestException("no bids in the response"))

    val bids = res.seatbid.map { seatBids =>
      for {
        seatbid <- seatBids
        rtbBid  <- seatbid.bid
      } yield {
        val params    = Map(AUCTION_PRICE -> rtbBid.price.toString)
        val nurl      = rtbBid.nurl.map(substitute(params))
        val burl      = rtbBid.burl.map(substitute(params))
        val rtbSeatId = seatbid.seat

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
          nurl = nurl,
          burl = burl,
          dsp = Some(name),
          adUnit = Some(adUnit),
          apiFramework = Some(Plc[P].apiFramework),
          bidId = res.bidid,
          seatId = rtbSeatId,
          ext = rtbBid.ext
        )
      }
    }
    bids.fold[BiddingResult](noBidsError)(bs => Right(bs.toList))
  }

  def parseRes(response: WSResponse): Either[String, BidResponse] = {
    logger.debug(s"Dirty response:\n" + new String(response.bodyAsBytes.toArray))
    val json     = parser.parseByteBuffer(response.bodyAsBytes.asByteBuffer).leftMap(_.message)
    val r        = json.flatMap(_.as[BidResponse].leftMap(_.message))
    lazy val str = if (r.isRight) "success" else s"failed with status: ${r.left.toOption.getOrElse("unknown")}"
    logger.debug(s"Parsing " + str)
    r
  }

  def nbr(response: BidResponse): Either[NoBidReason, BidResponse] = {
    logger.debug(s"BidResponse:" + response.asJson.pretty(printer))
    response.nbr.fold(response.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)
  }

  override def prepareAd[A: Adm](bid: Bid): Task[PbAd] = {
    val params    = Map(AUCTION_PRICE -> bid.price.toString)
    val impTrackers = bid.ext
      .flatMap(_.hcursor.get[List[String]]("imptrackers").toOption)
      .getOrElse(Nil)
      .map(substitute(params))


    val rawAdm   = bid.adm.toRight(Failure(FailureReason.RtbAdmMissingFailure, "missing adm"))
    val adm      = rawAdm.map(substitute(params)).flatMap(Adm[A].parse)

    val ad = adm
      .map(Adm[A].render)
      .map(PbAd(_, impTrackers))
      .toTry

    Task.fromTry(ad)
  }
}
