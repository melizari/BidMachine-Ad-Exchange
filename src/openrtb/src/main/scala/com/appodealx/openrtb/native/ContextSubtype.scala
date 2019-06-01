package com.appodealx.openrtb.native

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ContextSubtype(override val value: Int) extends IntEnumEntry with Serializable


object ContextSubtype extends IntEnum[ContextSubtype] {

  object MixedContent extends ContextSubtype(10)

  object Article extends ContextSubtype(11)

  object Video extends ContextSubtype(12)

  object Audio extends ContextSubtype(13)

  object Image extends ContextSubtype(14)

  object UserGenerated extends ContextSubtype(15)

  object Social extends ContextSubtype(20)

  object Email extends ContextSubtype(21)

  object Chat extends ContextSubtype(22)

  object Market extends ContextSubtype(30)

  object AppStore extends ContextSubtype(31)

  object ProductReviews extends ContextSubtype(32)

  val values = findValues

}
