package com.appodealx.openrtb.native

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class PlacementType(override val value: Int) extends IntEnumEntry with Serializable


object PlacementType extends IntEnum[PlacementType] {

  object Feed extends PlacementType(1)

  object Atomic extends PlacementType(2)

  object Outside extends PlacementType(3)

  object Recommendation extends PlacementType(4)

  object PlacementType500 extends PlacementType(500)

  object PlacementType501 extends PlacementType(501)

  val values = findValues

}