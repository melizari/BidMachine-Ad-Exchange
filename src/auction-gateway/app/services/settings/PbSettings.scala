package services.settings
import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

case class PbSettings(tmax: FiniteDuration, pbTmax: FiniteDuration)

object PbSettings {
  implicit val pbConfigLoader: ConfigLoader[PbSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    PbSettings(
      tmax = config.getInt("tMax").millisecond,
      pbTmax = config.getInt("pbTmax").millisecond
    )
  }
}
