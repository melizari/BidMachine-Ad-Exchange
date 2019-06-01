package com.appodealx.exchange.common.models.analytics

import enumeratum._

sealed abstract class AdType(override val entryName: String, val prettyValue: String) extends EnumEntry

object AdType extends Enum[AdType] {

  object Banner            extends AdType("banner", "Banner")
  object Mrec              extends AdType("mrec", "MREC")
  object Interstitial      extends AdType("interstitial", "Interstitial")
  object Native            extends AdType("native", "Native")
  object Video             extends AdType("skippable_video", "Skippable video")
  object NonSkippableVideo extends AdType("non_skippable_video", "Non skippable video")

  val values = findValues
}
