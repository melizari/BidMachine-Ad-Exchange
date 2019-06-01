package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class VideoPlacementType(override val value: Int) extends IntEnumEntry with Serializable


object VideoPlacementType extends IntEnum[VideoPlacementType] {

  object InStream extends VideoPlacementType(1)

  object InBanner extends VideoPlacementType(2)

  object InArticle extends VideoPlacementType(3)

  object InFeed extends VideoPlacementType(4)

  object Interstitial extends VideoPlacementType(5)

  val values = findValues

}
