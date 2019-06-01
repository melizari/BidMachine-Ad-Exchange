package services.auction.pb.adapters.pubnative.model

import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import enumeratum.values.{IntEnum, IntEnumEntry}

// Not used from response, only for consistency and future usage.
sealed abstract class AssetGroupId(override val value: Int) extends IntEnumEntry

object AssetGroupId extends IntEnum[AssetGroupId] with CirceEnumInstances {

  object NativeS extends AssetGroupId(2)

  object NativeM extends AssetGroupId(5)

  object NativeL extends AssetGroupId(16)

  object BannerS extends AssetGroupId(9) // 320x50
  object BannerM extends AssetGroupId(7) // 300x250
  object BannerL extends AssetGroupId(13) // 320x480
  object HtmlBannerS extends AssetGroupId(10) // 320x50
  object HtmlBannerM extends AssetGroupId(8) // 300x250
  object HtmlBannerL extends AssetGroupId(21) // 320x480
  object VastM extends AssetGroupId(4) // 320x480
  object VastL extends AssetGroupId(15) // 320x480


  def bannerBySize(w: Int, h: Int) =
    (w, h) match {
      case (320, 50) => BannerS
      case (300, 250) => BannerM
      case (320, 480) => BannerL
      case _ => throw new NoSuchElementException(s"banner asset group id not found for ${w}exchange$h")
    }

  def htmlBannerBySize(w: Int, h: Int) =
    (w, h) match {
      case (320, 50) => HtmlBannerS
      case (300, 250) => HtmlBannerM
      case (320, 480) => HtmlBannerL
      case _ => throw new NoSuchElementException(s"html banner asset group id not found for ${w}exchange$h")
    }

  def vastBySize(w: Int, h: Int) =
    (w, h) match {
      case (300, 250) => VastM
      case (320, 480) => VastL
      case _ => throw new NoSuchElementException(s"vast asset group id not found for ${w}exchange$h")
    }

  override def values = findValues
}
