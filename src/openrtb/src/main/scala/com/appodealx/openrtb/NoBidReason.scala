package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class NoBidReason(override val value: Int) extends IntEnumEntry with Serializable


object NoBidReason extends IntEnum[NoBidReason] {

  object UnknownError extends NoBidReason(0)

  object TechnicalError extends NoBidReason(1)

  object InvalidRequest extends NoBidReason(2)

  object KnownWebSpider extends NoBidReason(3)

  object SuspectedNonHumanTraffic extends NoBidReason(4)

  object ProxyIP extends NoBidReason(5)

  object UnsupportedDevice extends NoBidReason(6)

  object BlockedPublisherOrSite extends NoBidReason(7)

  object UnmatchedUser extends NoBidReason(8)

  object DailyReaderCap extends NoBidReason(9)

  object DailyDomainCap extends NoBidReason(10)

  val values = findValues

}
