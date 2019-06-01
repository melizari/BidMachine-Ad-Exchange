package services.auction.pb.adapters.criteo.s2s

import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import io.circe.syntax._
import io.circe.{Printer, parser}
import models.auction.NoBidReason.{NoFill, ParsingError, RequestException}
import models.auction.{AdRequest, AdUnit, AdapterResult, Bid, BiddingResult}
import play.api.Logger
import play.api.http.{ContentTypes, Status}
import play.api.libs.ws.{WSClient, WSResponse}
import play.mvc.Http.HeaderNames
import play.mvc.Http.HeaderNames.USER_AGENT
import services.auction.pb.adapters.BidderCaller
import services.auction.pb.adapters.criteo.s2s.model.rq.BidRequest
import services.auction.pb.adapters.criteo.s2s.model.rs.BidResponse
import services.settings.PbSettings
import services.settings.criteo.CriteoS2SSettings

import cats.ApplicativeError
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.functor._

class WsBidderCaller[F[_]](ws: WSClient, settings: CriteoS2SSettings, pbSettings: PbSettings)(
  implicit E: Execute[F], A: ApplicativeError[F, Throwable]
) extends BidderCaller[F] {

  private val printer = Printer.noSpaces.copy(dropNullValues = true)
  private val logger  = Logger(this.getClass)

  override def apply[P: Plc](adRequest: AdRequest[P],
                             bidder: Bidder,
                             adUnits: List[AdUnit]): F[AdapterResult] = {

    val biddingResult = for {
      zoneId     <- settings.getZoneId(adRequest, bidder)
      bidRequest <- BidRequest(adRequest, zoneId)
      adUnit     <- adUnits.headOption
    } yield requestBid(adRequest, bidRequest, bidder, adUnit)

    biddingResult.getOrElse(A.pure((false, Left(RequestException("could not create valid WS request")))))
  }

  private def requestBid[P: Plc](adRequest: AdRequest[P], bidRequest: BidRequest, bidder: Bidder, adUnit: AdUnit) = {
    val headers = List(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON) ++
      adRequest.device.ua.map(USER_AGENT -> _) ++ adRequest.device.ip.map("X-Client-IP" -> _)

    val placementId  = settings.getZoneId(adRequest, bidder: Bidder)
    val apiFramework = Plc[P].apiFramework

    val wsRequest = ws
      .url(bidder.endpoint.toString)
      .withMethod("POST")
      .addHttpHeaders(headers: _*)
      .withBody(bidRequest.asJson.pretty(printer))
      .withRequestTimeout(pbSettings.pbTmax)

    val wsResponseToBidResponse: WSResponse => AdapterResult = {
      case res if res.status == Status.OK && res.body.nonEmpty =>
        logger.debug(s"Criteo S2S: Fill with body: ${res.body}")

        (false, responseToBiddingResult(res, adUnit, placementId, apiFramework))
      case res =>
        logger.debug(s"Criteo S2S: NoFill with status: ${res.status} and body: ${res.body}")

        (false, Left(NoFill))
    }

    Execute[F]
      .deferFuture(wsRequest.execute())
      .map(wsResponseToBidResponse)
      .handleError { e =>
        logger.error(e.getMessage, e)
        (false, Left(RequestException(e.getMessage)))
      }
  }

  private def responseToBiddingResult(wsr: WSResponse,
                                      adUnit: AdUnit,
                                      placementId: Option[String],
                                      apiFramework: String): BiddingResult = {
    val json        = parser.parse(wsr.body).leftMap(_.message)
    val bidResponse = json.flatMap(_.as[BidResponse].leftMap(_.message))

    logBidResponse(wsr, bidResponse)

    bidResponse
      .leftMap(ParsingError)
      .flatMap(toBids(adUnit, placementId, apiFramework))
  }

  private def toBids(adUnit: AdUnit, placementId: Option[String], apiFramework: String)(r: BidResponse) = {

    val bids = r.seatbid
      .flatMap(_.bid)
      .filter(_.adm.isDefined)
      .map { b =>
        Bid(
          price = b.bidprice,
          adm = b.adm,
          impid = Some(b.impid),
          adomain = Some(b.adomain :: Nil),
          crid = Some(b.crid),
          placementId = placementId,
          customResponse = b.adm.map(_.asJson),
          adUnit = Some(adUnit),
          apiFramework = Some(apiFramework),
          dsp = Some("criteo")
        )
      }

    bids match {
      case Nil => Left(RequestException("no_bids"))
      case bs  => Right(bs)
    }
  }

  private def logBidResponse(wsr: WSResponse, bidResponse: Either[String, BidResponse]): Unit = {
    def parsingResultMsg =
      if (bidResponse.isRight) "success" else s"failed with status: ${bidResponse.left.toOption.getOrElse("unknown")}"

    logger.debug(s"BidResponse: " + new String(wsr.bodyAsBytes.toArray))
    logger.debug("Parsing " + parsingResultMsg)
  }
}
