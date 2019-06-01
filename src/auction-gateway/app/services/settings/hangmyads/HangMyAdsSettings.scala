package services.settings.hangmyads

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.concurrent.duration.FiniteDuration

case class HangMyAdsSettings(internalId: Long, cpmScheme: HangMyAdsCpmScheme, ttl: FiniteDuration)

object HangMyAdsSettings {
  implicit val configLoader: ConfigLoader[HangMyAdsSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    val id     = config.getLong("internal-id")
    val ttl    = FiniteDuration(config.getDuration("ttl").getSeconds, TimeUnit.SECONDS)
    val scheme = HangMyAdsCpmScheme(config.getString("cpm-scheme"))
      .getOrElse(throw new RuntimeException("invalid HangMyAds settings"))

    HangMyAdsSettings(id, scheme, ttl)
  }
}

sealed trait HangMyAdsCpmScheme

object HangMyAdsCpmScheme {

  def apply(s: String): Option[HangMyAdsCpmScheme] = s.toLowerCase match {
    case "min"     => Some(Min)
    case "current" => Some(Current)
    case _         => None
  }

  case object Min     extends HangMyAdsCpmScheme
  case object Current extends HangMyAdsCpmScheme
}
