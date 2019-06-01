import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.persistance.buyer.repos._
import com.appodealx.exchange.settings.persistance.seller.repos._
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.softwaremill.macwire.wire
import models.auction.AdRequest
import monix.eval.Task
import services.auction.pb.adapters.openx.OpenXEnabledSettings
import services.auction.pb.adapters.pubmatic.PubmaticSettings
import services.settings.criteo.{CriteoEnabled, CriteoEnabledSettings}
import services.settings.rubicon.{RubiconConfiguration, RubiconSettings}
import services.settings.{AdNetworkSettings, AdNetworksRepo, AlwaysEnabledNetwork, PbSettings}
import services.{AppodealAppRepo, SellerRepoImpl}

trait RepoComponents {
  self: PostgresComponents
    with ScalaCacheComponents
    with MonixComponents
    with LagomServiceClientComponents =>

  import scalacache.Monix.modes.task

  lazy val pbSettings: PbSettings = configuration.get[PbSettings]("settings.default")

  lazy val settingsService = serviceClient.implement[SettingsService]

  lazy val rubiconConfig = configuration.get[RubiconConfiguration]("settings.pb.rubicon")

  lazy val rubiconSettings: RubiconSettings = wire[RubiconSettings]

  lazy val pbPubmaticSettings: PubmaticSettings = configuration.get[PubmaticSettings]("settings.pubmatic.pb")
  lazy val rtbPubmaticSettings: PubmaticSettings = configuration.get[PubmaticSettings]("settings.pubmatic.rtb")

  private lazy val criteoEnabledSettings: CriteoEnabledSettings =
    configuration.get[CriteoEnabledSettings]("settings.pb.criteo")

  lazy val openXEnabledSettings: OpenXEnabledSettings = configuration.get[OpenXEnabledSettings]("settings.pb.openx")

  lazy val alwaysEnabledNetwork: AdNetworkSettings = new AlwaysEnabledNetwork()

  lazy val adNetworkSettings =
      "criteo"     -> CriteoEnabled(criteoEnabledSettings) ::
      "criteo_s2s" -> CriteoEnabled(criteoEnabledSettings) ::
      "rubicon"    -> rubiconSettings ::
      "openx"      -> openXEnabledSettings ::
      "pubmatic"   -> pbPubmaticSettings ::
      "hangmyads"  -> alwaysEnabledNetwork :: Nil

  lazy val bannerAdSpaceRepoInst = wire[BannerAdSpaceRepo[Task]]
  lazy val nativeAdSpaceRepoInst = wire[NativeAdSpaceRepo[Task]]
  lazy val videoAdSpaceRepoInst  = wire[VideoAdSpaceRepo[Task]]
  lazy val bannerBidderRepoInst  = wire[BannerBidderRepo[Task]]
  lazy val videoBidderRepoInst   = wire[VideoBidderRepo[Task]]
  lazy val nativeBidderRepoInst  = wire[NativeBidderRepo[Task]]

  lazy val appRepo       = wire[AppodealAppRepo[Task]]
  lazy val sellerRepo    = wire[SellerRepoImpl[Task]]
  lazy val adNetworkRepo = wire[AdNetworksRepo]
  lazy val adSpaceRepo   = wire[AdSpaceRepoImpl[Task]]
  lazy val bidderRepo    = wire[BidderRepoImpl[Task]]
}
