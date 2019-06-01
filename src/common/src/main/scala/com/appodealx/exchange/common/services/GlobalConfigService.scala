package com.appodealx.exchange.common.services

import com.appodealx.exchange.common.models.GlobalConfig
import com.appodealx.exchange.common.models.dao.GlobalConfigDAO
import play.api.Configuration
import scalacache.Mode
import scalacache.caffeine.CaffeineCache
import scalacache.redis._

import cats.Monad
import cats.data.OptionT
import cats.syntax.option._

import scala.concurrent.duration.Duration

trait GlobalConfigService[F[_]] {
  def read: F[GlobalConfig]
  def update(gsp: GlobalConfig): F[GlobalConfig]
}

class GlobalConfigServiceImpl[F[_]: Mode: Monad](conf: Configuration,
                                                 settingsDao: GlobalConfigDAO[F],
                                                 localCache: CaffeineCache[GlobalConfig],
                                                 redisCache: RedisCache[GlobalConfig])
    extends GlobalConfigService[F]
    with RedisSerialization {

  private val ttlRedis           = Duration.create(conf.get[String]("settings.cache.redis.ttl"))
  private val ttlLocal           = Duration.create(conf.get[String]("settings.cache.local.ttl"))
  private val marathonAppId      = conf.get[String]("marathon-app-id")
  private val key                = s"gs/$marathonAppId/v${GlobalConfig.version}"
  private val defaultTMax        = conf.get[Int]("settings.default.tMax")
  private val forceNoFillDefault = false
  private val defaultSettings    = GlobalConfig(defaultTMax.some, forceNoFillDefault.some)

  override def read = {
    def fetchSettings = OptionT(settingsDao.find).getOrElseF(update(defaultSettings))

    localCache
      .cachingF(key)(ttlLocal.some)(redisCache.cachingF(key)(ttlRedis.some)(fetchSettings))
  }

  override def update(gsp: GlobalConfig) = {
    val tmax           = gsp.tMax.getOrElse(defaultTMax)
    val forceNoFill    = gsp.forceNoFill.getOrElse(forceNoFillDefault)
    val globalSettings = gsp.copy(tmax.some, forceNoFill.some)

    settingsDao update globalSettings
  }
}
