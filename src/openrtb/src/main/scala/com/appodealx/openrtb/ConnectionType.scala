package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ConnectionType(override val value: Int) extends IntEnumEntry with Serializable


object ConnectionType extends IntEnum[ConnectionType] {

  object Unknown extends ConnectionType(0)

  object Ethernet extends ConnectionType(1)

  object Wifi extends ConnectionType(2)

  object CellularUnknownGen extends ConnectionType(3)

  object Cellular2G extends ConnectionType(4)

  object Cellular3G extends ConnectionType(5)

  object Cellular4G extends ConnectionType(6)

  val values = findValues

}