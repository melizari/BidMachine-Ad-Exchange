package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class VolumeNormalizationMode(override val value: Int) extends IntEnumEntry with Serializable


object VolumeNormalizationMode extends IntEnum[VolumeNormalizationMode] {

  object None extends VolumeNormalizationMode(0)

  object AdVolumeAverageNormalizedToContent extends VolumeNormalizationMode(1)

  object AdVolumePeakNormalizedToContent extends VolumeNormalizationMode(2)

  object AdLoudnessNormalizedToContent extends VolumeNormalizationMode(3)

  object CustomVolumeNormalization extends VolumeNormalizationMode(4)

  val values = findValues

}