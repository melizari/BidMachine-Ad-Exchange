package com.appodealx.exchange.common.models.auction

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class AuctionType(override val value: Int) extends IntEnumEntry with Serializable

object AuctionType extends IntEnum[AuctionType]{

  object FirstPrice extends AuctionType(1)

  object SecondPrice extends AuctionType(2)

  override def values = findValues
}