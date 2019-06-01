import akka.actor.ActorSystem
import play.api.Configuration
import redis.RedisClient

import scala.concurrent.duration.FiniteDuration
import scala.util.Try


trait RedisClientComponents {
  def actorSystem: ActorSystem

  def configuration: Configuration

  lazy val redisClient = {
    val redisQpsHost = configuration.get[String]("redis.host")
    val redisQpsPort = Try(configuration.get[String]("redis.port").toInt).toOption.getOrElse(6379)

    RedisClient(
      host = redisQpsHost,
      port = redisQpsPort,
      name = "ExchangeAdTrackerRedisClient",
      connectTimeout = Some(FiniteDuration(1000, "millisecond"))
    )(actorSystem)
  }
}
