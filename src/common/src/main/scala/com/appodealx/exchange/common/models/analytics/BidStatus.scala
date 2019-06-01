package com.appodealx.exchange.common.models.analytics

import enumeratum._


sealed abstract class BidStatus(override val entryName: String) extends EnumEntry

object BidStatus extends Enum[BidStatus] {

  object Win extends BidStatus("win")
  object Loss extends BidStatus("loss")

  val values = findValues

}
