package services

import play.api.ConfigLoader.configLoader


case class DatacenterMetadataSettings(dcid: String)

object DatacenterMetadataSettings {

  implicit val datacenterMetadataSettingsConfLoader = configLoader.map { conf =>
    DatacenterMetadataSettings(
      conf.getString("id")
    )
  }
}
