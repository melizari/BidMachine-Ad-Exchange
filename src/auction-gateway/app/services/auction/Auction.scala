package services.auction

import com.appodealx.exchange.common.models.analytics.CallbackContext
import com.appodealx.exchange.common.models.auction.Macros.{AUCTION_CURRENCY, _}
import com.appodealx.exchange.common.models.auction.{Adm, Plc, Protocol}
import com.appodealx.exchange.common.models.{AppId, PublisherId}
import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import com.appodealx.openrtb.LossReason
import models.Ad
import models.auction.{AdRequest, AuctionResult, ClearedBid, CpmOps}

import scala.util.Try

trait Auction[F[_]] {

  def perform[P: Plc](req: AdRequest[P]): F[AuctionResult]

  def prepareAd[A: Adm, P: Plc](meta: (AdRequest[P], Match), clearedBid: ClearedBid): F[Ad]

}

object Auction {

  // Returns (sspIncome, exchangeFee)
  def splitRevenue(req: AdRequest[_], clearingPrice: Double): (Double, Double) = {
    val fee    = req.sellerFee.map(fee => ((clearingPrice * fee) / 100).roundCPM).getOrElse(0.0)
    val income = (clearingPrice - fee).roundCPM
    (income, fee)
  }

  def substitutionParams(requestId: String, clearedBid: ClearedBid) = {
    val (clearingPrice, bid) = clearedBid

    Map(
      AUCTION_PRICE    -> clearingPrice.toString,
      AUCTION_ID       -> requestId,
      AUCTION_BID_ID   -> bid.bidId.getOrElse(""),
      AUCTION_SEAT_ID  -> bid.seatId.getOrElse(""),
      AUCTION_AD_ID    -> bid.adid.getOrElse(""),
      AUCTION_IMP_ID   -> bid.impid.getOrElse(""),
      AUCTION_CURRENCY -> "USD",
    )
  }

  def loseSubstitutionParams(requestId: String, clearedBid: ClearedBid) =
    substitutionParams(requestId, clearedBid) +
      (AUCTION_LOSS -> LossReason.LostToHigherBid.value.toString)

  def makeContext[P: Plc](meta: (AdRequest[P], Match), clearedBid: ClearedBid): CallbackContext = {
    import com.appodealx.exchange.common.utils.SdkVersionParser

    val (req, Match(agency, bidder, _)) = meta
    val (clearingPrice, bid)            = clearedBid

    val (income, fee) = Auction.splitRevenue(req, clearingPrice)

    val size = Plc[P].size(req.ad)

    CallbackContext(
      bidRequestId = req.id,
      impId = req.impId,
      originalBidFloor = req.sellerBidFloor,
      bidFloor = req.bidFloor,
      bidPrice = bid.price,
      clearingPrice = clearingPrice,
      exchangeFee = fee,
      sspIncome = income,
      appBundle = req.app.bundle,
      appId = req.app.id.flatMap(id => Try(id.toLong).toOption.map(AppId)),
      appIdRaw = req.app.id,
      appName = req.app.name,
      appVersion = req.app.ver,
      sspAuctionType = req.at.map(_.value),
      country = req.device.geo.flatMap(_.country).map(CountryParser.parse),
      deviceOs = req.device.os.map(_.toLowerCase),
      deviceOsVersion = req.device.osv,
      deviceIp = req.device.ip,
      deviceIpV6 = req.device.ipv6,
      deviceConnectionType = req.device.connectiontype,
      adType = Some(req.adType),
      adSize = size.map(s => s"${s.width}x${s.height}"),
      agencyId = agency.id,
      externalAgencyId = agency.externalId,
      externalPublisherId = req.app.publisher.flatMap(_.id.flatMap(PublisherId.fromString)),
      ifa = req.device.ifa,
      agencyName = Some(agency.title),
      bidderName = Some(bidder.title),
      sdkName = req.sdk,
      sdkVersion = req.sdkVersion,
      displayManager = bid.adUnit.map(_.sdk).orElse(Some(Plc[P].apiFramework)),
      displayManagerVersion = bid.adUnit.map(_.sdkVer),
      adomain = bid.adomain,
      cid = bid.cid,
      crid = bid.crid,
      isNewSdkVersion = req.sdkVersion.map(_.isNewSdkVersion),
      externalCampaignImageId = req.externalCampaignImageId,
      sellerId = req.sellerId,
      sellerName = req.sellerName,
      adNetwork = Some(bidder.protocol != Protocol.OpenRTB),
      adNetworkName = bid.adUnit.map(_.sdk),
      adNetworkPlacementId = bid.placementId,
      gdpr = req.gdpr,
      adSpaceId = req.adSpaceId.map(_.value),
      sesN = req.sesN,
      impN = req.impN
    )
  }
}
