package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class LocationType(override val value: Int) extends IntEnumEntry with Serializable


object LocationType extends IntEnum[LocationType] {

  object GPS extends LocationType(1)

  object IP extends LocationType(2)

  object UserProvided extends LocationType(3)

  val values = findValues

}