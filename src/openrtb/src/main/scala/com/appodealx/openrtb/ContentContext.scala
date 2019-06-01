package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class ContentContext(override val value: Int) extends IntEnumEntry with Serializable


object ContentContext extends IntEnum[ContentContext] {

  object Video extends ContentContext(1)

  object Game extends ContentContext(2)

  object Music extends ContentContext(3)

  object Application extends ContentContext(4)

  object Text extends ContentContext(5)

  object Other extends ContentContext(6)

  object Unknown extends ContentContext(7)

  val values = findValues

}
