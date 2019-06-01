package services.auction.pb.adapters.pubnative.model

import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import enumeratum.values.{StringEnum, StringEnumEntry}


sealed abstract class MetaType(override val value: String) extends StringEnumEntry

object MetaType extends StringEnum[MetaType] with CirceEnumInstances {

  object Points extends MetaType("points")

  object RevenueModel extends MetaType("revenuemodel")

  object CampaignId extends MetaType("campaignid")

  object CreativeId extends MetaType("creativeid")

  object ContentInfo extends MetaType("contentinfo")

  object BundleId extends MetaType("bundleid")


  val values = findValues

}
