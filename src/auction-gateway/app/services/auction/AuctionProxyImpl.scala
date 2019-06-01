package services.auction

import akka.util.ByteString
import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.analytics.{AuctionAnalyticEntity, BidStatus}
import com.appodealx.exchange.common.models.auction.{Adm, AgencyId, Plc, Protocol}
import com.appodealx.exchange.common.models.rtb.Version
import com.appodealx.exchange.common.services.SubstitutionService
import com.appodealx.exchange.common.utils.jsoniter.byteStringOfJson
import com.appodealx.exchange.common.utils.{analytics, CountryParser}
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import com.appodealx.openrtb.BidRequest
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import models.Ad
import models.auction.{AdRequest, AuctionItem, AuctionItemList, ClearedBid, HasPrice, NoBidReason}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.libs.ws.WSClient
import services.analytics.AnalyticsService
import services.analytics.AnalyticsServiceImpl.undefined

import cats.effect.Concurrent
import cats.effect.syntax.concurrent._
import cats.instances.list._
import cats.instances.tuple._
import cats.syntax.alternative._
import cats.syntax.bifunctor._
import cats.syntax.either._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.{Applicative, Monad, Parallel}

import scala.concurrent.duration.Duration
import scala.util.{Random, Try}

class AuctionProxyImpl[F[_]: Concurrent: Execute: Parallel[?[_], F]](
  settings: AuctionProxySettings,
  analyticsService: AnalyticsService[F],
  substitutionService: SubstitutionService,
  http: WSClient
) extends AuctionProxy[F] {

  import AuctionProxyImpl._

  private val logger = Logger(getClass)

  def perform[A: Adm, P: Plc](requests: List[AdRequest[P]], auctions: List[Auction[F]]) =
    if (!settings.multiFloors) {
      performAuction(requests.head, auctions)
    } else {
      val sortedRequests  = requests.sortBy(_.bidFloor)(Ordering[Double].reverse)
      val limitedRequests = sortedRequests.take(settings.floorsLimit)

      Monad[F].tailRecM(limitedRequests) {
        case req :: tail =>
          performAuction(req, auctions).map {
            case Some(ad) => Right(Some(ad))
            case None     => Left(tail)
          }

        case Nil => Monad[F].pure(Right(None))
      }
    }

  def call(url: String, timeout: Duration) = {
    for {
      _ <- Execute[F].deferFuture(http.url(url).withRequestTimeout(timeout).get)
      _ = logger.debug(s"Notified $url")
    } yield ()
  }.handleError { e =>
    Logger.warn(s"Error while notifying $url: ${e.getLocalizedMessage}")
  }

  private def performAuction[A: Adm, P: Plc](adRequest: AdRequest[P], auctions: List[Auction[F]]): F[Option[Ad]] =
    for {
      results               <- auctions.parTraverse(announce(adRequest))
      (nobids, auctionBids) = results.separate
      sortedBids            = sortAuctionWithBids(auctionBids)

      (win, losses) = sortedBids match {
        case (winner, winBid :: lostBid) :: restLosses =>
          (Some(winner -> winBid), (winner -> lostBid) :: restLosses)
        case _ => (None, sortedBids)
      }

      winEntity     = win.map(_._2).map(makeBidEntity(isWinner = true, adRequest))
      lostEntities  = losses.flatMap(_._2).map(makeBidEntity(isWinner = false, adRequest))
      noBidEntities = nobids.flatMap(_._2).map(makeNoBidEntity(adRequest))
      _             <- analyticsService.send(winEntity.toList ++ lostEntities ++ noBidEntities).start.void
      _             <- notifyLosers(losses, adRequest, settings.`loss-notification-timeout`).parSequence.start.void

      ad <- win match {
             case Some((auction, ((meta, _), clearedBid))) =>
               for {
                 ad        <- auction.prepareAd[A, P]((adRequest, meta), clearedBid)
                 winParams = Auction.substitutionParams(adRequest.id, clearedBid)
                 nurl      = ad.nurl.map(substitutionService.substitute(winParams))
                 _         <- notify(nurl.toList, settings.`win-notification-timeout`).start.void
               } yield Option(ad)
             case _ => Applicative[F].pure[Option[Ad]](None)
           }

    } yield ad

  private def makeBidEntity[P: Plc](isWinner: Boolean, adRequest: AdRequest[P])(item: AuctionItem[ClearedBid]) =
    makeEntity(adRequest, item.map(_.asRight[NoBidReason]), isWinner)

  private def makeNoBidEntity[P: Plc](adRequest: AdRequest[P])(item: AuctionItem[NoBidReason]) =
    makeEntity(adRequest, item.map(_.asLeft[ClearedBid]), isWinner = false)

  private def announce[P: Plc](adRequest: AdRequest[P])(auction: Auction[F]) = {
    def assoc[B](b: B) = auction -> b
    auction.perform(adRequest).map(_.bimap(assoc, assoc))
  }

  private def notifyLosers[P: Plc](
    losers: List[(Auction[F], AuctionItemList[ClearedBid])],
    adRequest: AdRequest[P],
    timeout: Duration
  ) =
    losers.map {
      case (_, items) =>
        val lurls = items.flatMap {
          case (_, clearedBid @ (_, bid)) =>
            val lossParams = Auction.loseSubstitutionParams(adRequest.id, clearedBid)
            bid.lurl.map(substitutionService.substitute(lossParams))
        }

        notify(lurls, timeout)
    }
}

object AuctionProxyImpl {

  import HasPrice.syntax._

  def sortAuctionWithBids[A, B: HasPrice](auctionsWithBids: List[(A, List[B])]) = {
    def headBidPrice(bids: List[B]) = bids.headOption.map(_.price).getOrElse(0.0)

    val shuffledAuctions = Random.shuffle(auctionsWithBids)
    val sortedAuctions   = shuffledAuctions.sortBy { case (_, b) => headBidPrice(b) }(Ordering[Double].reverse)

    sortedAuctions
  }

  // Beware DateTime.now side effect!
  def makeEntity[P: Plc](
    adRequest: AdRequest[P],
    item: AuctionItem[Either[NoBidReason, ClearedBid]],
    isWinner: Boolean
  ): AuctionAnalyticEntity = {

    import analytics.priceLevel

    val ((meta, cached), biddingResult) = item
    val clearedBid                      = biddingResult.toOption
    val clearingPrice                   = clearedBid.map(_._1)
    val bid                             = clearedBid.map(_._2)

    AuctionAnalyticEntity(
      timestamp = DateTime.now(DateTimeZone.UTC),
      mediationId = adRequest.impId,
      bidRequestId = adRequest.id,
      adType = adRequest.adType,
      sspAuctionType = adRequest.at.map(_.value),
      adSize = adRequest.sizeString,
      adSpaceId = adRequest.adSpaceId.map(_.value),
      appExternalId = adRequest.app.id.flatMap(id => Try(id.toLong).toOption),
      appPublisherExternalId = adRequest.app.publisher.flatMap(_.id.flatMap(id => Try(id.toLong).toOption)),
      appName = adRequest.app.name,
      appPublisherName = adRequest.app.publisher.flatMap(_.name),
      appBundle = adRequest.app.bundle.getOrElse(undefined),
      appCategories = adRequest.app.cat.map(_.map(analytics.convertCategory)),
      appVersion = adRequest.app.ver,
      sellerId = adRequest.sellerId,
      sellerName = adRequest.sellerName,
      bidderAgencyId = meta.agency.id,
      bidderAgencyExternalId = meta.agency.externalId,
      bidderAgencyName = Some(meta.agency.title),
      bidderId = meta.bidder.id,
      bidderName = Some(meta.bidder.title),
      bidderAdProfileId = meta.profile.id,
      bidderRtbVersion = Version.`2.3`,
      bidStatus = if (isWinner) BidStatus.Win else BidStatus.Loss,
      bidResponseStatus = Some(biddingResult.fold(_.prettyValue, _ => "bid")),
      bidAdomain = bid.flatMap(_.adomain),
      bidBundle = bid.flatMap(_.bundle),
      bidCategories = bid.flatMap(_.cat),
      bidAttributes = bid.flatMap(_.attr),
      bidImpressionUrl = bid.flatMap(_.iurl),
      bidCampaignId = bid.flatMap(_.cid),
      bidCreativeId = bid.flatMap(_.crid),
      bidCreativeRating = bid.flatMap(_.qagmediarating),
      bidHasAdm = bid.exists(_.adm.isDefined),
      bidFloorLevel = priceLevel(adRequest.bidFloor),
      bidPriceLevel = bid.map(_.price).map(priceLevel).getOrElse(undefined),
      clearingPriceLevel = if (isWinner) clearingPrice.map(priceLevel) else None,
      clearingPrice = if (isWinner) clearingPrice else None,
      requestBlockedCategories = adRequest.bcat,
      requestBlockedAdvertisers = adRequest.badv,
      requestBlockedAttributes = Plc[P].battr(adRequest.ad),
      deviceOs = adRequest.device.os.flatMap(Platform.fromString),
      deviceOsVersion = adRequest.device.osv,
      deviceCarrier = adRequest.device.carrier,
      deviceMake = adRequest.device.make,
      deviceModel = adRequest.device.model,
      deviceType = adRequest.device.devicetype,
      deviceIfa = adRequest.device.ifa,
      deviceIp = adRequest.device.ip,
      deviceIpV6 = adRequest.device.ipv6,
      deviceConnectionType = adRequest.device.connectiontype,
      mediationSdkName = adRequest.sdk,
      mediationSdkVersion = adRequest.sdkVersion,
      displayManager = bid.flatMap(_.adUnit.map(_.sdk)),
      displayManagerVersion = bid.flatMap(_.adUnit.map(_.sdkVer)),
      country = adRequest.device.geo.flatMap(_.country).map(CountryParser.parse),
      coppa = adRequest.coppa.getOrElse(false),
      test = adRequest.test.getOrElse(false),
      adNetwork = meta.bidder.protocol != Protocol.OpenRTB,
      adNetworkName = bid.flatMap(_.adUnit.map(_.sdk)),
      adNetworkPlacementId = bid.flatMap(_.placementId),
      isBid = biddingResult.isRight,
      isCached = cached,
      isUnderPrice = bid.exists(_.price < adRequest.bidFloor),
      sessionNumber = adRequest.sesN,
      impressionNumber = adRequest.impN,
      ipLocationService = adRequest.device.geo.flatMap(_.ipservice),
      dcid = adRequest.dcid
    )
  }

  def bidRequestModifier[P: Plc](meta: Match, request: BidRequest, cachedBidRequest: ByteString)(
    implicit codec: JsonValueCodec[BidRequest]
  ) =
    if (meta.agency.id.contains(AgencyId(84))) {
      val modifiedPublisher  = request.app.flatMap(_.publisher.map(_.copy(id = Some("157800"))))
      val modifiedApp        = request.app.map(_.copy(publisher = modifiedPublisher))
      val modifiedBidRequest = request.copy(app = modifiedApp)

      (modifiedBidRequest, byteStringOfJson(modifiedBidRequest))
    } else {
      (request, cachedBidRequest)
    }
}
