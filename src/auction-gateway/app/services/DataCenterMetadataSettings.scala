package services

import play.api.ConfigLoader.configLoader


case class DataCenterMetadataSettings(dcid: String)

object DataCenterMetadataSettings {

  implicit val confLoader = configLoader.map { conf =>
    DataCenterMetadataSettings(
      conf.getString("id")
    )
  }
}

