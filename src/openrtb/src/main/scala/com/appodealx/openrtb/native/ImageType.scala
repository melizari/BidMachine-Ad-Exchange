package com.appodealx.openrtb.native

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ImageType(override val value: Int) extends IntEnumEntry with Serializable


object ImageType extends IntEnum[ImageType] {

  object Icon extends ImageType(1)

  object Logo extends ImageType(2) // deprecated

  object Main extends ImageType(3)

  val values = findValues

}
