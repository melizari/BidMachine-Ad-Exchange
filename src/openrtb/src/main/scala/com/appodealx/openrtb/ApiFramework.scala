package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ApiFramework(override val value: Int) extends IntEnumEntry with Serializable


object ApiFramework extends IntEnum[ApiFramework] {

  object VPAID_1 extends ApiFramework(1)

  object VPAID_2 extends ApiFramework(2)

  object MRAID_1 extends ApiFramework(3)

  object ORMMA extends ApiFramework(4)

  object MRAID_2 extends ApiFramework(5)

  object MRAID_3 extends ApiFramework(6)

  val values = findValues

}