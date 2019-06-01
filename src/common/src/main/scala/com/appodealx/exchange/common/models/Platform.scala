package com.appodealx.exchange.common.models

import enumeratum._

sealed abstract class Platform(override val entryName: String, val prettyValue: String) extends EnumEntry with Serializable

object Platform extends Enum[Platform] {

  object Android extends Platform("android", "Android")
  object iOS extends Platform("ios", "iOS")
  object Amazon extends Platform("amazon", "Amazon")

  val values = findValues

  def fromString(string: String): Option[Platform] = Platform.withNameOption(string.toLowerCase)

}