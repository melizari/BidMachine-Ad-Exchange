import com.appodealx.exchange.common.models.auction.AuctionType.{FirstPrice, SecondPrice}
import com.appodealx.exchange.common.services.GlobalConfigServiceImpl
import com.softwaremill.macwire.wire
import controllers.auction.{PbAuctionController, Rtb2AuctionController}
import controllers.helpers.CustomControllerComponents
import models.MarketplaceType
import models.MarketplaceType.{Open, OpenFirstPrice, Pb}
import monix.eval.Task
import play.api.Configuration
import services.auction.Auction
import services.auction.pb._
import services.auction.rtb.RtbAuction
import services.auction.rtb3.DeviceWithGeoEnricher
import services.callback.builders._
import services.callback.injectors.LegacyCallbackInjectorImpl
import services.callback.injectors.banner.LegacyBannerCallbackInjectorImpl
import services.callback.injectors.nast.LegacyNativeCallbackInjectorImpl
import services.callback.injectors.vast.LegacyVideoCallbackInjectorImpl
import services.settings.SellerAuctionsSettings

trait LegacyAuctionComponents {
  self: RedisClientComponents
    with RepoComponents
    with GeoIpComponents
    with SharedComponents =>

  def configuration: Configuration

  implicit def globalConfigService: GlobalConfigServiceImpl[Task]

  def customControllerComponents: CustomControllerComponents

  private lazy val impressionCallbackBuilder  = wire[LegacyImpressionCallbackBuilder]
  private lazy val clickCallbackBuilder       = wire[LegacyClickCallbackBuilder]
  private lazy val finishCallbackBuilder      = wire[LegacyFinishCallbackBuilder]
  private lazy val fillsCallbackBuilder       = wire[LegacyFillsCallbackBuilder]
  private lazy val customEventCallbackBuilder = wire[CustomEventLegacyCallbackBuilder]
  private lazy val errorCallbackBuilder       = wire[LegacyErrorCallbackBuilder]

  private lazy val bannerCallbackInjector                       = wire[LegacyBannerCallbackInjectorImpl]
  private lazy val nativeCallbackInjector                       = wire[LegacyNativeCallbackInjectorImpl]
  private lazy val videoCallbackInjector                        = wire[LegacyVideoCallbackInjectorImpl]
  private lazy val callbackInjector: LegacyCallbackInjectorImpl = wire[LegacyCallbackInjectorImpl]

  private lazy val sellerAuctionsSettings: SellerAuctionsSettings[Task] = SellerAuctionsSettings[Task](
    sellerPerMarketplacesSettings = sellersMarketplacesSettings,
    auctionsPerMarketplace = auctionPerMarketPlaceType,
    defaultAuctions = rtbAuction :: pbAuction :: Nil
  )


  private lazy val pbAuction: PbAuction[Task] = wire[PbAuction[Task]]

  private lazy val rtbAuction = new RtbAuction[Task](
    adapter = bidderClient,
    bidderRepo = bidderRepo,
    injector = callbackInjector,
    ss = substitutionService,
    auctionType = SecondPrice,
    bidRequestModifiers = rtbRequestModifiers,
    bidderFilters = rtbAdRequestFilters
  )

  private lazy val rtbAuctionFirstPrice = new RtbAuction[Task](
    adapter = bidderClient,
    bidderRepo = bidderRepo,
    injector = callbackInjector,
    ss = substitutionService,
    auctionType = FirstPrice,
    bidRequestModifiers = rtbRequestModifiers,
    bidderFilters = rtbAdRequestFilters,
  )

  private lazy val auctionPerMarketPlaceType: Map[MarketplaceType, Auction[Task]] =
    Map(
      Open           -> rtbAuction,
      OpenFirstPrice -> rtbAuctionFirstPrice,
      Pb             -> pbAuction
    )

  private lazy val pbAdRequestBuilder: PbAdRequestsBuilderImpl[Task] = wire[PbAdRequestsBuilderImpl[Task]]

  private lazy val pbAuctionService: PbAuctionService[Task] = new PbAuctionServiceImpl[Task](
    auction = auctionProxy,
    auctionsPerType = auctionPerMarketPlaceType,
    defaultAuctions = rtbAuction :: pbAuction :: Nil,
    adRequestBuilder = pbAdRequestBuilder
  )

  lazy val pbAdController = wire[PbAuctionController]

  lazy val rtbAdController = new Rtb2AuctionController(
    adSpaceRepo = adSpaceRepo,
    sellerRepo = sellerRepo,
    cc = customControllerComponents,
    auction = auctionProxy,
    sellerAuctionsSettings = sellerAuctionsSettings,
    dcMetadata = datacenterMetadataSettings,
    enrichDeviceWithGeo = DeviceWithGeoEnricher(geoIpServiceProxyImp),
  )
}
