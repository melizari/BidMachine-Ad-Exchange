package com.appodealx.openrtb.native

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class DataType(override val value: Int) extends IntEnumEntry with Serializable


object DataType extends IntEnum[DataType] {

  object Sponsored extends DataType(1)

  object Desc extends DataType(2)

  object Rating extends DataType(3)

  object Likes extends DataType(4)

  object Download extends DataType(5)

  object Price extends DataType(6)

  object SalePrice extends DataType(7)

  object Phone extends DataType(8)

  object Address extends DataType(9)

  object Desc2 extends DataType(10)

  object DisplayUrl extends DataType(11)

  object CtaText extends DataType(12)

  val values = findValues

}