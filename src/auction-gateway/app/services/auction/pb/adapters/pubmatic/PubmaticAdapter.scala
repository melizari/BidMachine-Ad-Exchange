package services.auction.pb.adapters.pubmatic

import com.appodealx.exchange.common.models.auction.Macros._
import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.common.models.{Failure, FailureReason}
import com.appodealx.exchange.common.services.SubstitutionService
import com.appodealx.openrtb.AuctionType.FirstPrice
import com.appodealx.openrtb.BidResponse
import io.circe.parser
import io.circe.syntax._
import models.PbAd
import models.auction.NoBidReason._
import models.auction.{AdRequest, AdUnit, Bid, BiddingResult, NoBidReason}
import monix.eval.Task
import play.api.Logger
import play.api.http.ContentTypes
import play.api.http.Status._
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import services.auction.pb.DefaultAdapter
import services.settings.PbSettings

import cats.syntax.either._

class PubmaticAdapter(ws: WSClient,
                      substitutionService: SubstitutionService,
                      settings: PubmaticSettings,
                      pbSettings: PbSettings)
    extends DefaultAdapter[Task]("pubmatic")
    with CirceModelsInstances {

  private val logger = Logger(this.getClass)

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) =
    adUnits.headOption match {
      case Some(a) => doRequest(bidder, request, a).map((false, _))
      case None    => Task.pure((false, Left(NoBidReason.NoFill)))
    }

  override def prepareAd[A: Adm](bid: Bid) = {
    val params = Map(
      AUCTION_PRICE    -> bid.price.toString,
      AUCTION_BID_ID   -> bid.bidId.getOrElse(""),
      AUCTION_AD_ID    -> bid.adid.getOrElse(""),
      AUCTION_IMP_ID   -> bid.impid.getOrElse(""),
      AUCTION_SEAT_ID  -> bid.seatId.getOrElse(""),
      AUCTION_CURRENCY -> "USD",
    )

    val rawAdm = bid.adm.toRight(Failure(FailureReason.RtbAdmMissingFailure, "missing adm"))

    val adm = rawAdm
      .map(substitute(params))
      .flatMap(Adm[A].parse)

    val ad = adm
      .map(Adm[A].render)
      .map(m => PbAd(markup = m))

    Task.fromEither(ad)
  }

  private def doRequest[P: Plc](bidder: Bidder, request: AdRequest[P], adUnit: AdUnit): Task[BiddingResult] = {
    val bidRequest = toBidRequest(request)
    val endpoint   = bidder.endpoint.toString
    val reqBody    = bidRequest.asJson.pretty(printer)

    logger.debug(s"Pubmatic bid request: $reqBody")

    Task.fromFuture {
      ws.url(bidder.endpoint.toString())
        .withMethod("POST")
        .addHttpHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)
        .withBody(reqBody)
        .withRequestTimeout(pbSettings.pbTmax)
        .execute()
    }.map {
      case r if (r.status == NO_CONTENT || r.status == OK) && r.body.isEmpty =>
        logger.debug(s"NO FILL: `$name` with status `${r.status}` and empty body for $endpoint")
        NoFill.asLeft
      case r if r.status == OK =>
        logger.debug(s"FILL: `$name` with status `OK` for $endpoint")
        logger.debug(s"Response: " + r.body)
        parseResponseToBiddingResult(r, adUnit, request.id)
      case r =>
        logger.error(s"ERROR: `$name` with status: ${r.status} for $endpoint")
        UnexpectedResponse(r.status).asLeft
    }.onErrorRecover {
      case e: Exception =>
        logger.error(e.getMessage, e)
        RequestException(e.getClass.getName).asLeft
    }
  }

  private def parseResponseToBiddingResult[P: Plc](res: WSResponse, adUnit: AdUnit, reqId: String): BiddingResult = {

    def bidResponse =
      parser
        .parse(res.body)
        .leftMap(_.message)
        .flatMap(_.as[BidResponse].leftMap(_.message))
        .leftMap(ParsingError)

    def checkNbr(response: BidResponse) =
      response.nbr.fold(response.asRight[NoBidReason])(RtbNoBidReason(_).asLeft)

    def toBids(response: BidResponse) = {
      val noBidsError = Left(NoBidReason.RequestException("no bids in the response"))

      val bids = response.seatbid.map { seatBids =>
        for {
          seatbid <- seatBids
          rtbBid  <- seatbid.bid
        } yield {

          val rtbSeatId = seatbid.seat

          val params    = Map(
            AUCTION_PRICE    -> rtbBid.price.toString,
            AUCTION_ID       -> reqId,
            AUCTION_BID_ID   -> rtbBid.id,
            AUCTION_SEAT_ID  -> rtbSeatId.getOrElse(""),
            AUCTION_AD_ID    -> rtbBid.adid.getOrElse(""),
            AUCTION_IMP_ID   -> rtbBid.impid,
            AUCTION_CURRENCY -> "USD",
          )
          val nurl      = rtbBid.nurl.map(substitute(params))
          val burl      = rtbBid.burl.map(substitute(params))


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
            seatId = rtbSeatId,
            ext = rtbBid.ext
          )
        }
      }

      bids.fold[BiddingResult](noBidsError)(bs => Right(bs.toList))
    }

    bidResponse
      .flatMap(checkNbr)
      .flatMap(toBids)
  }

  private def toBidRequest[P: Plc](request: AdRequest[P]) = {
    val app         = request.app
    val appId       = app.id.flatMap(settings.enabledApps.get)
    val publisher   = app.publisher.map(_.copy(id = Some(settings.publisherId)))
    val modifiedApp = app.copy(id = appId, publisher = publisher)

    request
      .copy(app = modifiedApp)
      .bidRequest
      .copy(at = Some(FirstPrice))
  }

  private def substitute(params: Map[String, String])(s: String) = substitutionService.substitute(params)(s)
}
