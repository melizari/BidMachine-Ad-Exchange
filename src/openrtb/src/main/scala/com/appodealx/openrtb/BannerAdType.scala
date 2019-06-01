package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class BannerAdType(override val value: Int) extends IntEnumEntry with Serializable


object BannerAdType extends IntEnum[BannerAdType] {

  object XHTMLTextAd extends BannerAdType(1)

  object XHTMLBannerAd extends BannerAdType(2)

  object JavaScriptAd extends BannerAdType(3)

  object Iframe extends BannerAdType(4)

  val values = findValues

}
