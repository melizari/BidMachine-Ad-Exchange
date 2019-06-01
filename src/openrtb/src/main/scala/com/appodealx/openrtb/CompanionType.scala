package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class CompanionType(override val value: Int) extends IntEnumEntry with Serializable


object CompanionType extends IntEnum[CompanionType] {

  object StaticResource extends CompanionType(1)

  object HTMLResource extends CompanionType(2)

  object IframeResource extends CompanionType(3)

  val values = findValues

}