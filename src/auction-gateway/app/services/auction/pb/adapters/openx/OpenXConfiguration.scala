package services.auction.pb.adapters.openx

import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.concurrent.duration._

final case class OpenXConfiguration(`notify-timeout`: FiniteDuration)

object OpenXConfiguration {
  implicit val configLoader: ConfigLoader[OpenXConfiguration] = (rootConfig: Config, path: String) => {
    val openXConf = rootConfig.getConfig(path)

    OpenXConfiguration(`notify-timeout` = openXConf.getLong("notify-ttl-ms").millisecond)
  }
}
