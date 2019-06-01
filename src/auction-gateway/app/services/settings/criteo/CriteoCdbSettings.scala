package services.settings.criteo
import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.concurrent.duration.FiniteDuration

case class CriteoCdbSettings(zones: Map[String, Int], ttl: FiniteDuration)

object CriteoCdbSettings {

  implicit val configLoader: ConfigLoader[CriteoCdbSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    val ttl = FiniteDuration(config.getDuration("ttl").getSeconds, TimeUnit.SECONDS)

    val cdbConfig = config.getConfig("cdb")
    val sizes = Set("320x50", "728x90", "320x480", "480x320", "768x1024", "1024x768")
    val zones = sizes.map(s => s -> cdbConfig.getString(s"zones.banner.$s").toInt).toMap

    CriteoCdbSettings(zones, ttl)
  }
}
