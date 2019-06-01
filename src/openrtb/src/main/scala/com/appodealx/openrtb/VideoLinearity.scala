package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class VideoLinearity(override val value: Int) extends IntEnumEntry with Serializable


object VideoLinearity extends IntEnum[VideoLinearity] {

  object Linear extends VideoLinearity(1)

  object NonLinear extends VideoLinearity(2)

  val values = findValues

}