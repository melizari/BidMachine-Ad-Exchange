package services.settings
import com.typesafe.config.Config
import play.api.ConfigLoader

final case class DataCenterEndpointsSettings(eu: List[String], us: List[String])

object DataCenterEndpointsSettings {

  implicit val configLoader: ConfigLoader[DataCenterEndpointsSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    DataCenterEndpointsSettings(
      eu = config.getString("eu-endpoints").split(",").toList,
      us = config.getString("us-endpoints").split(",").toList
    )
  }
}
