package com.appodealx.exchange.settings.models.buyer

import enumeratum._


sealed class ExternalAdType(override val entryName: String) extends EnumEntry {}

object ExternalAdType extends Enum[ExternalAdType]{

  object Banner extends ExternalAdType("banner_320")
  object BannerMrec extends ExternalAdType("banner_mrec")
  object Interstitial extends ExternalAdType("banner")
  object Native extends ExternalAdType("native")
  object Video extends ExternalAdType("video")
  object RewardedVideo extends ExternalAdType("rewarded_video")

  val values = findValues

}
