package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class PlaybackCessationMode(override val value: Int) extends IntEnumEntry with Serializable


object PlaybackCessationMode extends IntEnum[PlaybackCessationMode] {

  object OnCompletion extends PlaybackCessationMode(1)

  object OnLeavingViewport extends PlaybackCessationMode(2)

  object FloatUntilCompletion extends PlaybackCessationMode(3)

  val values = findValues

}
