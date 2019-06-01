package services.auction.pb.adapters.pubnative.model

import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import enumeratum.values.{StringEnum, StringEnumEntry}


sealed abstract class AssetType(override val value: String) extends StringEnumEntry

object AssetType extends StringEnum[AssetType] with CirceEnumInstances {

  object Icon extends AssetType("icon")

  object Banner extends AssetType("banner")

  object Title extends AssetType("title")

  object Description extends AssetType("description")

  object Rating extends AssetType("rating")

  object Cta extends AssetType("cta")

  object StandardBanner extends AssetType("standardbanner")

  object HtmlBanner extends AssetType("htmlbanner")

  object Vast2 extends AssetType("vast2")

  val values = findValues

}
