import com.appodealx.exchange.common.models.SecureCallbackParams
import com.appodealx.exchange.common.models.auction.{BidderId, Protocol}
import com.appodealx.exchange.common.services.crypto.{JcaSigner, JcaSignerSettings, Signer}
import com.appodealx.exchange.common.services.kafka.CirceKafkaProducer
import com.appodealx.exchange.common.services.{ParamsSigner, SubstitutionService}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.softwaremill.macwire.wire
import io.circe.jawn.JawnParser
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding.PercentEncoder
import io.lemonlabs.uri.encoding.PercentEncoder.QUERY_CHARS_TO_ENCODE
import monix.eval.Task
import monix.execution.Scheduler
import services.DataCenterMetadataSettings
import services.analytics.AnalyticsServiceImpl
import services.auction.pb.AdapterRegistry
import services.auction.pb.adapters.CachingBidderCaller
import services.auction.pb.adapters.adcolony.AdColonyAdapter
import services.auction.pb.adapters.applovin.AppLovinAdapter
import services.auction.pb.adapters.criteo.cdb.{CriteoAdapter => CdbCriteo}
import services.auction.pb.adapters.criteo.s2s.{RateLimitingBidderCaller, WsBidderCaller, CriteoAdapter => S2SCriteo}
import services.auction.pb.adapters.hangmyads.{HangMyAdsAdapter, HangMyAdsBidderCaller}
import services.auction.pb.adapters.mytarget.MyTargetAdapter
import services.auction.pb.adapters.openx.{OpenXAdapter, OpenXConfiguration}
import services.auction.pb.adapters.pubmatic.PubmaticAdapter
import services.auction.pb.adapters.pubnative.PubNativeAdapter
import services.auction.pb.adapters.rubicon.RubiconAdapter
import services.auction.pb.adapters.smaato.SmaatoAdapter
import services.auction.pb.adapters.tapjoy.TapjoyAdapter
import services.auction.pb.adapters.vungle.VungleAdapter
import services.auction.rtb.adapters.OpenRtbAdapter
import services.auction.rtb.filters.{AdRequestFilter, PubmaticFilter}
import services.auction.rtb.reqmodifiers.{BidRequestModifier, PubmaticModifier}
import services.auction.{AuctionProxyImpl, AuctionProxySettings}
import services.callback.markup.banner.HtmlMarkupBuilder
import services.callback.markup.video.VastMarkupBuilder
import services.settings.SellersMarketplacesSettings
import services.settings.criteo.{CriteoCdbSettings, CriteoS2SSettings}
import services.settings.hangmyads.HangMyAdsSettings

import cats.Parallel

trait SharedComponents extends KafkaComponents {
  self: RepoComponents
  with LagomServiceClientComponents
  with RedisClientComponents
  with ScalaCacheComponents =>

  import scalacache.Monix.modes._

  implicit def scheduler: Scheduler

  implicit val parallelTask = implicitly[Parallel[Task, Task.Par]].asInstanceOf[Parallel[Task, Task]]

  implicit val uriConfig = UriConfig.default.copy(queryEncoder = PercentEncoder(QUERY_CHARS_TO_ENCODE + '$'))

  lazy val auctionSettings = configuration.get[AuctionProxySettings]("settings.auction")
  lazy val auctionProxy: AuctionProxyImpl[Task] = wire[AuctionProxyImpl[Task]]

  lazy val datacenterMetadataSettings = configuration.get[DataCenterMetadataSettings]("settings.data-center-metadata")

  lazy val circeKafkaProducer = wire[CirceKafkaProducer]

  lazy val analyticsService = wire[AnalyticsServiceImpl[Task]]

  lazy val bidderClient: OpenRtbAdapter[Task] = wire[OpenRtbAdapter[Task]]

  lazy val jcaSignerSettings = JcaSignerSettings(configuration.get[String]("play.http.secret.key"))
  lazy val jcaSigner: Signer = wire[JcaSigner]

  lazy val paramsSignService   = new ParamsSigner(jcaSigner, SecureCallbackParams.Names)
  lazy val substitutionService = wire[SubstitutionService]

  lazy val jawnParser = wire[JawnParser]

  lazy val sellersMarketplacesSettings =
    configuration.get[SellersMarketplacesSettings]("settings.marketplace")

  lazy val pubmaticFilter: AdRequestFilter = new PubmaticFilter(rtbPubmaticSettings)

  lazy val rtbRequestModifiers: Map[BidderId, BidRequestModifier] = Map(
    BidderId(180) -> PubmaticModifier(rtbPubmaticSettings)
  )

  lazy val rtbAdRequestFilters: Map[BidderId, AdRequestFilter] = Map(
    BidderId(180) -> pubmaticFilter
  )

  lazy val criteoCdbSettings: CriteoCdbSettings = configuration.get[CriteoCdbSettings]("settings.pb.criteo")
  lazy val criteoS2SSettings: CriteoS2SSettings = configuration.get[CriteoS2SSettings]("settings.pb.criteo")

  lazy val openXConfiguration: OpenXConfiguration = configuration.get[OpenXConfiguration]("settings.pb.openx")

  //S2S Criteo Client
  lazy val wsCaller: WsBidderCaller[Task] = wire[WsBidderCaller[Task]]
  lazy val rateLimitingCaller: RateLimitingBidderCaller[Task] =
    new RateLimitingBidderCaller[Task]("criteo_s2s", redisClient)(wsCaller)
  lazy val s2SCriteo: S2SCriteo = new S2SCriteo(rateLimitingCaller)

  // HangMyAds
  private lazy val hangMyAdsSettings: HangMyAdsSettings = configuration.get[HangMyAdsSettings]("settings.pb.hangmyads")
  private lazy val hangMyAdsBidsCaller: HangMyAdsBidderCaller[Task] =
    new HangMyAdsBidderCaller(wsClient, hangMyAdsSettings, pbSettings)
  private lazy val hangMyAdsCachingCaller: CachingBidderCaller[Task] =
    new CachingBidderCaller(hangMyAdsBidsCaller, adapterResponsesCache, hangMyAdsSettings.ttl)
  private lazy val hangMyAdsAdapter: HangMyAdsAdapter = new HangMyAdsAdapter(hangMyAdsCachingCaller)


  lazy val adNetworkClientRegistry: AdapterRegistry[Task] = {

    val formerMailRuClient = new MyTargetAdapter(wsClient, pbSettings, name = Protocol.Mailru.value)
    val myTargetClient     = new MyTargetAdapter(wsClient, pbSettings, name = Protocol.MyTarget.value)
    val pubmaticAdapter    = new PubmaticAdapter(wsClient, substitutionService, pbPubmaticSettings, pbSettings)

    Map(
      "vungle"     -> wire[VungleAdapter],
      "applovin"   -> wire[AppLovinAdapter],
      "adcolony"   -> wire[AdColonyAdapter],
      "criteo"     -> wire[CdbCriteo],
      "criteo_s2s" -> s2SCriteo,
      "mailru"     -> formerMailRuClient,
      "my_target"  -> myTargetClient,
      "tapjoy"     -> wire[TapjoyAdapter],
      "smaato"     -> wire[SmaatoAdapter],
      "pubnative"  -> wire[PubNativeAdapter],
      "rubicon"    -> wire[RubiconAdapter],
      "openx"      -> wire[OpenXAdapter],
      "pubmatic"   -> pubmaticAdapter,
      "hangmyads"  -> hangMyAdsAdapter
    ).get _
  }


  lazy val htmlMarkupBuilders: List[HtmlMarkupBuilder] = Nil
  lazy val vastMarkupBuilders: List[VastMarkupBuilder] = Nil
}
