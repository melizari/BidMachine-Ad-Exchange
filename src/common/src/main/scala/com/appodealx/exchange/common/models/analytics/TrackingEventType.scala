package com.appodealx.exchange.common.models.analytics

import enumeratum.values.{StringEnum, StringEnumEntry}


sealed abstract class TrackingEventType(val value: String, val prettyValue: String) extends StringEnumEntry

object TrackingEventType extends StringEnum[TrackingEventType] {

  case object ImpressionEvent extends TrackingEventType("i", "impression")

  case object ClickEvent extends TrackingEventType("c", "click")

  case object FinishEvent extends TrackingEventType("f", "finish")

  case object FillEvent extends TrackingEventType("fi", "fill")

  case object CustomEvent extends TrackingEventType("ce", "custom-event")

  case object ErrorVastEvent extends TrackingEventType("v", "VAST")

  case object ErrorMraidEvent extends TrackingEventType("m", "MRAID")

  case object ErrorNastEvent extends TrackingEventType("n", "NAST")

  def values = findValues

}