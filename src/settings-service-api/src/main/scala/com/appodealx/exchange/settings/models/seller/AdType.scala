package com.appodealx.exchange.settings.models.seller

import enumeratum.values.{StringEnum, StringEnumEntry}

sealed abstract class AdType(val value: String) extends StringEnumEntry

object AdType extends StringEnum[AdType] {

  case object Banner extends AdType("banner")
  case object Native extends AdType("native")
  case object Video extends AdType("video")

  def values = findValues
}