package com.appodealx.exchange.settings.models.seller

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class Platform(val value: String) extends StringEnumEntry

object Platform extends StringEnum[Platform] {

  case object iOS extends Platform("ios")
  case object Android extends Platform("android")

  val values = findValues
}