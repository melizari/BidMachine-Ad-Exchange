package services.settings.criteo

import com.typesafe.config.Config
import play.api.{ ConfigLoader, Configuration }

final case class CriteoEnabledSettings(enabledApps: Set[String])
object CriteoEnabledSettings {
  implicit val configLoader: ConfigLoader[CriteoEnabledSettings] = (rootConfig: Config, path: String) => {
    val conf = Configuration(rootConfig.getConfig(path))

    def getConfigValuesSet(path: String) =
      conf
        .getOptional[String](path)
        .filter(_.nonEmpty)
        .map(_.split(",").toSet)
        .getOrElse(Set.empty[String])

    val enabledApps  = getConfigValuesSet("enabled-apps")

    CriteoEnabledSettings(enabledApps)
  }
}
