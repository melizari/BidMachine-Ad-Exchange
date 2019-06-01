package services.auction.pb.adapters.openx

import com.appodealx.exchange.common.models.auction.Plc
import com.typesafe.config.Config
import models.auction.AdRequest
import play.api.{ConfigLoader, Configuration}
import services.settings.AdNetworkSettings

final case class OpenXEnabledSettings(enabledApps: Set[String]) extends AdNetworkSettings {
  override def enabled[T: Plc](request: AdRequest[T]) =
    request.app.bundle.exists(enabledApps.contains)
}

object OpenXEnabledSettings {
  implicit val configLoader: ConfigLoader[OpenXEnabledSettings] = (rootConfig: Config, path: String) => {
    val conf = Configuration(rootConfig.getConfig(path))

    def getConfigValuesSet(path: String) =
      conf
        .getOptional[String](path)
        .filter(_.nonEmpty)
        .map(_.split(",").toSet)
        .getOrElse(Set.empty[String])

    val enabledApps  = getConfigValuesSet("enabled-apps")

    OpenXEnabledSettings(enabledApps)
  }
}
