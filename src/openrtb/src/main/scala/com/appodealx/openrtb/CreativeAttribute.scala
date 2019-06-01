package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class CreativeAttribute(override val value: Int) extends IntEnumEntry with Serializable


object CreativeAttribute extends IntEnum[CreativeAttribute] {

  object AudioAdAutoPlay extends CreativeAttribute(1)

  object AudioAdUserInit extends CreativeAttribute(2)

  object ExpandableAutomatic extends CreativeAttribute(3)

  object ExpandableUserClick extends CreativeAttribute(4)

  object ExpandableUserRollover extends CreativeAttribute(5)

  object InBannerVideoAdAutoPlay extends CreativeAttribute(6)

  object InBannerVideoAdUserInit extends CreativeAttribute(7)

  object Pop extends CreativeAttribute(8)

  object Provocative extends CreativeAttribute(9)

  object EpilepsyWarning extends CreativeAttribute(10)

  object Surveys extends CreativeAttribute(11)

  object TextOnly extends CreativeAttribute(12)

  object UserInteractive extends CreativeAttribute(13)

  object AlertStyle extends CreativeAttribute(14)

  object HasAudionButton extends CreativeAttribute(15)

  object AdCanBeSkipped extends CreativeAttribute(16)

  object AdobeFlash extends CreativeAttribute(17)

  val values = findValues

}
