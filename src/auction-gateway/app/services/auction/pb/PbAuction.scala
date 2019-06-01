package services.auction.pb

import com.appodealx.exchange.common.models.analytics.ContextTrackers
import com.appodealx.exchange.common.models.auction.AuctionType.FirstPrice
import com.appodealx.exchange.common.models.auction.{Adm, Plc}
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import models.auction.NoBidReason.BelowPriceFloor
import models.auction.{AdRequest, AdUnit, AuctionMeta, Bid, ClearedBid, FirstPriceStrategy, Metadata, NoBidReason}
import models.{Ad, RequestHost}
import play.api.Logger
import services.auction.Auction
import services.callback.injectors.CallbackInjector
import services.settings.AdNetworksRepo

import cats.effect.Concurrent
import cats.instances.either._
import cats.instances.list._
import cats.instances.tuple._
import cats.syntax.alternative._
import cats.syntax.applicativeError._
import cats.syntax.bifunctor._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.syntax.traverse._
import cats.{MonadError, Parallel}

class PbAuction[F[_]: MonadError[?[_], Throwable]: Parallel[?[_], F]: Concurrent](
  bidderRepo: BidderRepo[F],
  injector: CallbackInjector,
  adapterRegistry: AdapterRegistry[F],
  adNetworkRepo: AdNetworksRepo,
) extends Auction[F] {

  private val logger = Logger(getClass)

  def prepareAd[A: Adm, P: Plc](meta: (AdRequest[P], BidderRepo.Match), clearedBid: ClearedBid) = {

    val (req, BidderRepo.Match(_, _, profile)) = meta

    implicit val requestHost: RequestHost = req.host

    val (clearingPrice, bid) = clearedBid
    val (income, _)          = Auction.splitRevenue(req, clearingPrice)
    val sdk                  = bid.adUnit.map(_.sdk)
    val adapter              = sdk.flatMap(adapterRegistry)

    val pbMetadata = Metadata(
      renderMetadata = req.metadata,
      `X-Appodeal-Price` = income,
      `X-Appodeal-Cache` = profile.allowCache.getOrElse(true),
      `X-Appodeal-Campaign-ID` = bid.cid,
      `X-Appodeal-Close-Time` = profile.allowCloseDelay.getOrElse(0),
      `X-Appodeal-Creative-ID` = bid.crid,
      `X-Appodeal-Bid-Request-ID` = req.id,
      `X-Appodeal-Displaymanager` = bid.apiFramework.orElse(bid.adUnit.map(_.sdk)),
      `X-Appodeal-Demand-Source` = bid.dsp,
      `X-Appodeal-Impression-ID` = bid.impid,
      `X-Appodeal-Identifier` = bid.adUnit.flatMap(_.externalId),
      `X-Appodeal-Ad-Type` = req.prettyAdType
    )

    adapter match {
      case Some(a) =>
        a.prepareAd[A](bid).map { pbAd =>
          val callbackContext = Auction.makeContext(meta, clearedBid)
          val delayedNurl     = bid.nurl.filter(_ => profile.delayedNotification)
          val nonDelayedNurl  = bid.nurl.filterNot(_ => profile.delayedNotification)
          val contextTrackers = ContextTrackers(delayedNurl, bid.burl, pbAd.impTrackers, pbAd.clickTrackers)
          val trackingEvents  = injector.events[A](callbackContext, contextTrackers)
          val metadata        = injector.mkMetadata[A](pbMetadata, callbackContext, trackingEvents)

          Ad(
            markup = pbAd.markup,
            size = Plc[P].size(req.ad),
            metadata = metadata,
            trackingEvents = trackingEvents,
            sspIncome = income,
            nurl = nonDelayedNurl
          )
        }

      case None =>
        MonadError[F, Throwable]
          .raiseError[Ad](new NoSuchElementException(s"no_client_found_for_name_${sdk.getOrElse("unknown")}"))
    }
  }

  def perform[P: Plc](request: AdRequest[P]) = {

    def announce(adapter: Adapter[F], meta: Match, adUnits: List[AdUnit]) = {
      val bidder = meta.bidder

      adapter
        .announce[P](bidder, request, adUnits)
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

    def execute(bids: List[(AuctionMeta, Bid)]) =
      FirstPriceStrategy.execute(request.bidFloor, bids) map { case (price, (meta, bid)) => (meta, (price, bid)) }

    def fetchAdapters =
      for {
        matches  <- bidderRepo.query(request.bidderRepoQuery, FirstPrice)
        adUnits  = request.adUnits ++ adNetworkRepo.enabledAdNetworks(request)
        sdkNames = adUnits.map(_.sdk).toSet
        _        = logMatchedBidders(matches, request, sdkNames)
      } yield {
        for {
          meta <- matches
          if sdkNames.contains(meta.bidder.protocol.value)
          bidder        = meta.bidder
          adapter       <- adapterRegistry(bidder.protocol.value)
          bidderAdUnits = adUnits.filter(_.sdk == bidder.protocol.value)
        } yield (adapter, meta, bidderAdUnits)
      }

    for {
      adapters                    <- fetchAdapters
      biddingResults              <- adapters.parTraverse((announce _).tupled)
      (nobids, bids)              = biddingResults.flatten.separate
      (validBids, bidsBelowFloor) = bids partition { case (_, bid) => bid.price >= request.bidFloor }
      nobidsBelowFloor            = bidsBelowFloor.map(_.map(_ => BelowPriceFloor))
      errors                      = nobids.filterNot(_._2 == NoBidReason.NoFill)
      clearedBids                 = execute(validBids)
    } yield (nobidsBelowFloor ++ errors, clearedBids)
  }

  private def logMatchedBidders[P: Plc](
    matchedBidders: List[Match],
    request: AdRequest[P],
    sdkNames: Set[String]
  ): Unit = {
    Logger.debug(s"PB AUCTION: MATCHED BIDDERS FROM DB ${matchedBidders.map(_.bidder.protocol.value)}")
    Logger.debug(s"PB AUCTION: REQUEST DISPLAY MANAGERS ${request.adUnits.map(_.sdk)}")
    Logger.debug(s"PB AUCTION: ENABLED AD NETWORKS $sdkNames")
  }
}
