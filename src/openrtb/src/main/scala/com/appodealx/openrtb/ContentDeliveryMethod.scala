package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ContentDeliveryMethod(override val value: Int) extends IntEnumEntry with Serializable


object ContentDeliveryMethod extends IntEnum[ContentDeliveryMethod] {

  object Streaming extends ContentDeliveryMethod(1)

  object Progressive extends ContentDeliveryMethod(2)

  val values = findValues

}