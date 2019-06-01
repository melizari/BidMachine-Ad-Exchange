package services.settings
import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.concurrent.duration.FiniteDuration

final case class GeoIpSettings(ttl: FiniteDuration)

object GeoIpSettings {

  implicit val configLoader: ConfigLoader[GeoIpSettings] = (rootConfig: Config, path: String) => {
    val config   = rootConfig.getConfig(path)
    val duration = config.getDuration("ttl")

    GeoIpSettings(
      ttl = FiniteDuration.apply(duration.toMillis, java.util.concurrent.TimeUnit.MILLISECONDS),
    )
  }
}