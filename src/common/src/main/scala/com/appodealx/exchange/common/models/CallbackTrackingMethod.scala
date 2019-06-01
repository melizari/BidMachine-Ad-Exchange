package com.appodealx.exchange.common.models

import enumeratum.values.{ IntEnum, IntEnumEntry }

import scala.util.Try

sealed abstract class CallbackTrackingMethod(override val value: Int, val prettyValue: String)
    extends IntEnumEntry
    with Serializable

object CallbackTrackingMethod extends IntEnum[CallbackTrackingMethod] {

  object Unknown extends CallbackTrackingMethod(0, "unknown")

  object BannerJs extends CallbackTrackingMethod(10, "js")

  object BannerPixel extends CallbackTrackingMethod(11, "pixel")

  object VastInLine extends CallbackTrackingMethod(20, "vast_in_line")

  object VastWrapper extends CallbackTrackingMethod(21, "vast_wrapper")

  object Native extends CallbackTrackingMethod(30, "native")

  object Header extends CallbackTrackingMethod(40, "header")

  override def values = findValues

  def withValueOpt(number: String) = Try(number.toInt).toOption.flatMap(super.withValueOpt)

}
