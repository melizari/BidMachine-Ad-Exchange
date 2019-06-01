package com.appodealx.openrtb.native

import enumeratum.values.{IntEnum, IntEnumEntry}

/**
  * The Layout ID of the native ad unit.
  *
  * Layout ID is to be deprecated since 1.0
  *
  * Below is a list of the core layouts described in the introduction above.
  *
  * @param value
  */
sealed abstract class LayoutType(override val value: Int) extends IntEnumEntry with Serializable

object LayoutType extends IntEnum[PlacementType] {

  object ContentWall extends LayoutType(1)

  object AppWall extends LayoutType(2)

  object NewsFeed extends LayoutType(3)

  object ChatList extends LayoutType(4)

  object Carousel extends LayoutType(5)

  object ContentStream extends LayoutType(6)

  object GridContent extends LayoutType(7)

  object LayoutType500 extends LayoutType(500)

  val values = findValues
}
