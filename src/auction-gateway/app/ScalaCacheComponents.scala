import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.circe.CirceRtbInstances
import com.appodealx.exchange.settings.models.seller.{BannerAdSpace, NativeAdSpace, Seller, VideoAdSpace}
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo
import com.appodealx.openrtb
import models.auction.BiddingResult
import play.api.Configuration
import scalacache.CacheConfig
import scalacache.caffeine.CaffeineCache
import scalacache.redis.RedisCache

trait ScalaCacheComponents extends CirceRtbInstances {

  import scalacache.serialization.binary._
  import scalacache.serialization.circe.codec

  def configuration: Configuration

  private val redisHost = configuration.get[String]("redis.host")
  private val redisPort = configuration.getOptional[Int]("redis.port").getOrElse(6379)

  private val adapterCacheHost = configuration.get[String]("redis.applovin.host")
  private val adapterCachePort = configuration.get[Int]("redis.applovin.port")

  lazy val redisAppCache = RedisCache[openrtb.App](redisHost, redisPort)(CacheConfig(), codec[openrtb.App])
  lazy val localAppCache = CaffeineCache[openrtb.App]

  lazy val redisConfigCache = RedisCache[GlobalConfig](redisHost, redisPort)
  lazy val localConfigCache = CaffeineCache[GlobalConfig]

  lazy val bannerAdSpaceCache = CaffeineCache[BannerAdSpace]
  lazy val videoAdSpaceCache  = CaffeineCache[VideoAdSpace]
  lazy val nativeAdSpaceCache = CaffeineCache[NativeAdSpace]

  lazy val biddersCache = CaffeineCache[List[BidderRepo.Match]]

  lazy val publisherCache = CaffeineCache[Seller]

  lazy val adapterResponsesCache = RedisCache[BiddingResult](adapterCacheHost, adapterCachePort)
}
