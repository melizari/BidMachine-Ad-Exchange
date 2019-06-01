import com.appodealx.exchange.common.models.auction.AuctionType.{FirstPrice, SecondPrice}
import com.softwaremill.macwire.wire
import controllers.auction.{InitController, Rtb3AuctionController}
import controllers.helpers.CustomControllerComponents
import models.MarketplaceType
import models.MarketplaceType.{Open, OpenFirstPrice, Pb}
import monix.eval.Task
import play.api.mvc.CustomPlayBodyParsers
import services._
import services.auction.Auction
import services.auction.pb.PbAuction
import services.auction.rtb.RtbAuction
import services.auction.rtb3._
import services.callback.builders._
import services.callback.injectors.CallbackInjectorImpl
import services.callback.injectors.banner.BannerCallbackInjectorImpl
import services.callback.injectors.nast.NativeCallbackInjectorImpl
import services.callback.injectors.vast.VideoCallbackInjectorImpl
import services.init.InitServiceImpl
import services.settings.SellerAuctionsSettings

trait AuctionComponents {
  self: RepoComponents
    with GlobalConfigComponents
    with GeoIpComponents
    with SharedComponents =>

  def customBodyParsers: CustomPlayBodyParsers

  def customControllerComponents: CustomControllerComponents

  private lazy val validationModule = wire[ValidationServiceImpl[Task]]

  private lazy val impressionCallbackBuilder = wire[ImpressionCallbackBuilder]
  private lazy val clickCallbackBuilder = wire[ClickCallbackBuilder]
  private lazy val closedCallbackBuilder = wire[ClosedCallbackBuilder]
  private lazy val loadedCallbackBuilder = wire[LoadedCallbackBuilder]
  private lazy val customEventCallbackBuilder = wire[CustomEventCallbackBuilder]
  private lazy val errorCallbackBuilder = wire[ErrorCallbackBuilder]
  private lazy val destroyedCallbackBuilder = wire[DestroyedCallbackBuilder]
  private lazy val viewableCallbackBuilder = wire[ViewableCallbackBuilder]

  private lazy val bannerCallbackInjector = wire[BannerCallbackInjectorImpl]
  private lazy val nativeCallbackInjector = wire[NativeCallbackInjectorImpl]
  private lazy val videoCallbackInjector = wire[VideoCallbackInjectorImpl]
  private lazy val callbackInjector: CallbackInjectorImpl = wire[CallbackInjectorImpl]

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
    bidderFilters = rtbAdRequestFilters
  )

  private lazy val pbAuction = wire[PbAuction[Task]]

  private lazy val bmSettings = configuration.get[BidMachineSettings]("settings.bm")

  private lazy val rtb3BidResponseMapper = new Rtb3BidResponseMapperImpl(AdcomAdMapperImpl, bmSettings)
  private lazy val rtb3BidReqUnpacker    = new Rtb3UnpackerImpl[Task](validationModule)

  private lazy val auctionPerMarketPlaceType: Map[MarketplaceType, Auction[Task]] =
    Map(
      Open           -> rtbAuction,
      OpenFirstPrice -> rtbAuctionFirstPrice,
      Pb             -> pbAuction
    )

  private lazy val sellerAuctionsSettings: SellerAuctionsSettings[Task] = SellerAuctionsSettings[Task](
    sellerPerMarketplacesSettings = sellersMarketplacesSettings,
    auctionsPerMarketplace = auctionPerMarketPlaceType,
    defaultAuctions = rtbAuction :: pbAuction :: Nil
  )

  lazy val rtb3AuctionService = new Rtb3Service[Task](
    unpacker = rtb3BidReqUnpacker,
    proxy = auctionProxy,
    sellerAuctionsSettings = sellerAuctionsSettings,
    requestBuilder = rtb3AdReqBuilder,
    resMapper = rtb3BidResponseMapper
  )

  private lazy val sellerBidFloorsSettings = configuration.get[SellerFloorsSettings]("settings.bm.custom")

  private lazy val rtb3AdReqBuilder: Rtb3AdRequestsBuilderImpl[Task] = new Rtb3AdRequestsBuilderImpl[Task](
    sellerRepo   = sellerRepo,
    globalConfig = globalConfigService,
    dcMetadata = datacenterMetadataSettings,
    enrichDeviceWithGeo = DeviceWithGeoEnricher(geoIpServiceProxyImp),
    getSellerIdFloors = sellerBidFloorsSettings.getBidFloorsBySellerId
  )

  lazy val initService = wire[InitServiceImpl[Task]]
  lazy val initController = wire[InitController]
  lazy val openRtb3AdController = wire[Rtb3AuctionController]
}
