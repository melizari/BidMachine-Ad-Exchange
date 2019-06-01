package services.settings

import com.appodealx.exchange.common.models.Id
import com.typesafe.config.Config
import models.MarketplaceType
import play.api.ConfigLoader
import services.settings.SettingsUtils.parseConfigStringOfOneToManyAssociations

final case class SellersMarketplacesSettings(settings: Map[Id, Set[MarketplaceType]])

object SellersMarketplacesSettings {

  implicit val configLoader: ConfigLoader[SellersMarketplacesSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    val rawSettings = parseConfigStringOfOneToManyAssociations(config.getString("marketplaces-by-seller"))

    val settings = rawSettings.get.map {
      case (id, marketplace) => (id, marketplace.map(MarketplaceType.withValue))
    }

    SellersMarketplacesSettings(settings)
  }
}
