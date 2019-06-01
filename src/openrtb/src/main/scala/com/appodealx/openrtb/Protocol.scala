package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class Protocol(override val value: Int) extends IntEnumEntry with Serializable


object Protocol extends IntEnum[Protocol] {

  object VAST_1 extends Protocol(1)

  object VAST_2 extends Protocol(2)

  object VAST_3 extends Protocol(3)

  object VAST_1_WRAPPER extends Protocol(4)

  object VAST_2_WRAPPER extends Protocol(5)

  object VAST_3_WRAPPER extends Protocol(6)

  object VAST_4 extends Protocol(7)

  object VAST_4_WRAPPER extends Protocol(8)

  object DAAST_1 extends Protocol(9)

  object DAAST_1_WRAPPER extends Protocol(10)

  val values = findValues

}
