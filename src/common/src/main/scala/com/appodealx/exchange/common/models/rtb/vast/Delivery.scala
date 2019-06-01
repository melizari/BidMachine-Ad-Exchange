package com.appodealx.exchange.common.models.rtb.vast


sealed abstract class Delivery(override val entryName: String) extends VastEnumEntry


object Delivery extends VastEnum[Delivery] {

  object Progressive extends Delivery("progressive")
  object Streaming extends Delivery("streaming")

  val values = findValues

}