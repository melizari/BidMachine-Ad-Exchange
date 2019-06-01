package services.auction.rtb

import akka.util.ByteString
import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.FailureReason.AdmPrepareFailure
import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, ContextTrackers}
import com.appodealx.exchange.common.models.auction.AuctionType.FirstPrice
import com.appodealx.exchange.common.models.auction._
import com.appodealx.exchange.common.models.dto.{Native, Video}
import com.appodealx.exchange.common.models.jsoniter.JsoniterRtbInstances
import com.appodealx.exchange.common.services.SubstitutionService
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import com.appodealx.openrtb.BidRequest
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.zafarkhaja.semver.Version
import models.{Ad, RequestHost}
import models.auction.NoBidReason.BelowPriceFloor
import models.auction._
import play.api.Logger
import services.auction.Auction
import services.auction.rtb.filters.AdRequestFilter
import services.auction.rtb.reqmodifiers.BidRequestModifier
import services.callback.injectors.CallbackInjector
import cats.effect.Concurrent
import cats.instances.either._
import cats.instances.list._
import cats.instances.tuple._
import cats.syntax.alternative._
import cats.syntax.applicativeError._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.syntax.traverse._
import cats.{MonadError, Parallel}

import scala.language.postfixOps
import scala.util.Try

class RtbAuction[F[_]: MonadError[?[_], Throwable]: Concurrent: Execute: Parallel[?[_], F]](
  adapter: Adapter[F],
  bidderRepo: BidderRepo[F],
  injector: CallbackInjector,
  ss: SubstitutionService,
  auctionType: AuctionType,
  bidRequestModifiers: Map[BidderId,BidRequestModifier],
  bidderFilters: Map[BidderId, AdRequestFilter])
extends Auction[F]
  with JsoniterRtbInstances {

  private val logger = Logger(getClass)

  private val strategy = auctionType match {
    case FirstPrice => FirstPriceStrategy
    case _          => SecondPriceStrategy
  }

  override def perform[P: Plc](request: AdRequest[P]) = {

    val bidFloor = request.bidFloor

    def execute(bids: List[(AuctionMeta, Bid)]) =
      strategy.execute(bidFloor, bids) map { case (price, (meta, bid)) => (meta, (price, bid)) }

    for {
      (nobids, bids)              <- callForBids(request)
      (validBids, bidsBelowFloor) = bids partition { case (_, bid) => bid.price >= bidFloor }
      nobidsBelowFloor            = bidsBelowFloor.map(_.map(_ => BelowPriceFloor))
      errors                      = nobids.filterNot(_._2 == NoBidReason.NoFill)
      clearedBids                 = execute(validBids)
    } yield (nobidsBelowFloor ++ errors, clearedBids)
  }

  private def callForBids[P: Plc](request: AdRequest[P]): F[(AuctionItemList[NoBidReason], AuctionItemList[Bid])] = {

    val bidRequest       = request.bidRequest
    val cachedBidRequest = ByteString(writeToArray[BidRequest](bidRequest))

    def printMatches(bidders: List[BidderRepo.Match]): Unit = {
      logger.debug("Matched bidders:")
      bidders.zipWithIndex.foreach { case (b, i) => logger.debug(s"   $i: $b") }
    }

    def announce(adapter: Adapter[F], meta: Match) = {
      val bidder = meta.bidder

      val (req, cachedReq) = (
        for {
          id     <- bidder.id
          modify <- bidRequestModifiers.get(id)
        } yield modify(meta, bidRequest)
      ).getOrElse((bidRequest, cachedBidRequest))

      adapter
        .announce(bidder, req, cachedReq)
        .map {
          case (cached, result) =>
            val m = (meta, cached)
            result.sequence.map(_.bimap(m -> _, m -> _))
        }
        .handleError {
          case e: Exception =>
            logger.error(e.getMessage, e)
            Left((meta, false), NoBidReason.RequestException(e.getMessage)) :: Nil
        }
    }

    for {
      matchedMeta    <- bidderRepo.query(request.bidderRepoQuery, auctionType, Some(Protocol.OpenRTB))
      filteredMeta   = performBidderCustomMatching(matchedMeta, request)
      _              = printMatches(filteredMeta)
      biddingResults <- filteredMeta.parTraverse(announce(adapter, _))
    } yield biddingResults.flatten.separate
  }

  def prepareAd[A: Adm, P: Plc](meta: (AdRequest[P], BidderRepo.Match), clearedBid: ClearedBid) = {

    val (req, _) = meta
    val (_, bid) = clearedBid

    val params = Auction.substitutionParams(req.id, clearedBid)

    bid.adm
      .map(substitute(params))
      .toRight(Failure(AdmPrepareFailure, "missing adm"))
      .flatMap(Adm[A].parse)
      .map(createAd(_, meta, clearedBid, params))
      .liftTo[F]
  }

  private def createAd[A: Adm, P: Plc](adm: A,
                                       meta: (AdRequest[P], BidderRepo.Match),
                                       clearedBid: ClearedBid,
                                       params: Map[String, String]) = {

    val (req, BidderRepo.Match(agency, _, profile)) = meta

    implicit val requestHost: RequestHost = req.host
    val plc                               = Plc[P]

    val (clearingPrice, bid) = clearedBid
    val (income, _)          = Auction.splitRevenue(req, clearingPrice)

    def mkRtbMetadata = {
      val adUnitId = req.adUnits
        .find(_.sdk == plc.apiFramework)
        .flatMap(_.externalId)

      Metadata(
        renderMetadata = req.metadata,
        `X-Appodeal-Displaymanager` = Some(plc.apiFramework),
        `X-Appodeal-Price` = income,
        `X-Appodeal-Demand-Source` = Some(agency.title),
        `X-Appodeal-Cache` = profile.allowCache.getOrElse(true),
        `X-Appodeal-Close-Time` = profile.allowCloseDelay.getOrElse(0),
        `X-Appodeal-Bid-Request-ID` = req.id,
        `X-Appodeal-Campaign-ID` = bid.cid,
        `X-Appodeal-Creative-ID` = bid.crid,
        `X-Appodeal-Impression-ID` = bid.impid,
        `X-Appodeal-Identifier` = adUnitId,
        `X-Appodeal-Adomain` = bid.adomain.map(_.mkString(",")),
        `X-Appodeal-Ad-Type` = req.prettyAdType
      )
    }

    def ad(repr: A, metadata: Metadata, events: AuctionTrackingEvents, nurl: Option[String]) =
      Ad(
        markup = Adm[A].render(repr),
        size = plc.size(req.ad),
        metadata = metadata,
        trackingEvents = events,
        sspIncome = income,
        nurl = nurl
      )

    val ctx            = Auction.makeContext(meta, clearedBid)
    val nurl           = bid.nurl.map(substitute(params))
    val burl           = bid.burl.map(substitute(params))
    val nonDelayedNurl = nurl.filterNot(_ => profile.delayedNotification)

    val contextTrackers = {
      val delayedNurl = nurl.filter(_ => profile.delayedNotification)
      val impTrackers = bid.ext
        .flatMap(_.hcursor.get[List[String]]("imptrackers").toOption)
        .getOrElse(Nil)
        .map(substitute(params))

      ContextTrackers(delayedNurl, burl, impTrackers)
    }

    val trackingEvents = injector.events[A](ctx, contextTrackers)

    val metadata = injector.mkMetadata[A](mkRtbMetadata, ctx, trackingEvents)

    def shouldInjectNative = {
      val `OS is iOS` = req.device.os.map(_.toLowerCase).contains("ios")
      val `Version less than 2.2.0` =
        req.sdkVersion.flatMap(v => Try(Version.valueOf(v)).toOption).exists(_.lessThan(Version.valueOf("2.2.0")))
      `OS is iOS` && `Version less than 2.2.0`
    }

    def admWithThirdPartyAndPixel(a: A) = {
      val withPixel = injector.injectLoadedEventPixel(a, ctx)
      injector.injectThirdPartyMarkup[A](withPixel, ctx)
    }

    def admWithCallbacks(a: A, errorsOnly: Boolean = false) =
      injector.injectMarkup(admWithThirdPartyAndPixel(a), ctx, contextTrackers, errorsOnly = errorsOnly)

    plc match {
      case s if s.is[Native] && shouldInjectNative && !metadata.renderMetadata =>
        ad(admWithCallbacks(adm), metadata, trackingEvents, nonDelayedNurl)

      case s if s.is[Video] && metadata.renderMetadata =>
        ad(admWithCallbacks(adm, errorsOnly = true), metadata, trackingEvents, nonDelayedNurl)

      case _ if metadata.renderMetadata =>
        ad(admWithThirdPartyAndPixel(adm), metadata, trackingEvents, nonDelayedNurl)

      case _ =>
        ad(admWithCallbacks(adm), metadata, trackingEvents, nonDelayedNurl)
    }
  }

  private def performBidderCustomMatching[P: Plc](matchedMeta: List[Match], request: AdRequest[P]) =
    matchedMeta.filter { m =>
      val bidderFilter = m.bidder.id.flatMap(bidderFilters.get)
      bidderFilter.isEmpty || bidderFilter.exists(_.filter(request, m))
    }

  private def substitute(params: Map[String, String])(s: String) = ss.substitute(s, params.toSeq: _*)
}
