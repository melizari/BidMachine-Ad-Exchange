package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class FeedType(override val value: Int) extends IntEnumEntry with Serializable


object FeedType extends IntEnum[FeedType] {

  object MusicService extends FeedType(1)

  object RadioBroadcast extends FeedType(2)

  object Podcast extends FeedType(3)

  val values = findValues

}