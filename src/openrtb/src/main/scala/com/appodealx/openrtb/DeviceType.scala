package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class DeviceType(override val value: Int) extends IntEnumEntry with Serializable


object DeviceType extends IntEnum[DeviceType] {

  object Mobile extends DeviceType(1)

  object PC extends DeviceType(2)

  object TV extends DeviceType(3)

  object Phone extends DeviceType(4)

  object Tablet extends DeviceType(5)

  object ConnectedDevice extends DeviceType(6)

  object SetTopBox extends DeviceType(7)

  val values = findValues

}