package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ExpandableDirection(override val value: Int) extends IntEnumEntry with Serializable


object ExpandableDirection extends IntEnum[ExpandableDirection] {

  object Left extends ExpandableDirection(1)

  object Right extends ExpandableDirection(2)

  object Up extends ExpandableDirection(3)

  object Down extends ExpandableDirection(4)

  object FullScreen extends ExpandableDirection(5)

  val values = findValues

}
