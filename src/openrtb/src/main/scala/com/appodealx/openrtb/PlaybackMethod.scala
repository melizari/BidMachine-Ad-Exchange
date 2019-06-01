package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class PlaybackMethod(override val value: Int) extends IntEnumEntry with Serializable


object PlaybackMethod extends IntEnum[PlaybackMethod] {

  object AutoPlaySoundOn extends PlaybackMethod(1)

  object AutoPlaySoundOff extends PlaybackMethod(2)

  object ClickToPlay extends PlaybackMethod(3)

  object MouseOver extends PlaybackMethod(4)

  object OnViewportEnterSoundOn extends PlaybackMethod(5)

  object OnViewportEnterSoundOff extends PlaybackMethod(6)

  val values = findValues

}
