package services.auction

import play.api.ConfigLoader._

import scala.concurrent.duration.FiniteDuration

case class AuctionProxySettings(multiFloors: Boolean, floorsLimit: Int, `win-notification-timeout`: FiniteDuration, `loss-notification-timeout`: FiniteDuration)

object AuctionProxySettings {

  implicit val auctionConfLoader = configLoader.map { conf =>
    val winMilliseconds = conf.getLong("win-notification-timeout-ms")
    val lossMilliseconds = conf.getLong("loss-notification-timeout-ms")

    AuctionProxySettings(
      conf.getBoolean("multi-floors-enabled"),
      conf.getInt("floors-limit"),
      FiniteDuration(winMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS),
      FiniteDuration(lossMilliseconds, java.util.concurrent.TimeUnit.MILLISECONDS)
    )
  }

}