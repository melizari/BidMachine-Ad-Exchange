package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class IpLocationService(override val value: Int) extends IntEnumEntry with Serializable


object IpLocationService extends IntEnum[IpLocationService] {

  object IpToLocation extends IpLocationService(1)

  object Neustar extends IpLocationService(2)

  object MaxMind extends IpLocationService(3)

  object NetAquity extends IpLocationService(4)

  case object Unknown extends IpLocationService(500)

  case object Sypex extends IpLocationService(501)

  val values = findValues

}
