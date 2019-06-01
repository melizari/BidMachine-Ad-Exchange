package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class AdPosition(override val value: Int) extends IntEnumEntry with Serializable


object AdPosition extends IntEnum[AdPosition] {

  object Unknown extends AdPosition(0)

  object AboveTheFold extends AdPosition(1)

  object MaybeNotVisible extends AdPosition(2)

  object BelowTheFold extends AdPosition(3)

  object Header extends AdPosition(4)

  object Footer extends AdPosition(5)

  object Sidebar extends AdPosition(6)

  object FullScreen extends AdPosition(7)

  val values = findValues

}