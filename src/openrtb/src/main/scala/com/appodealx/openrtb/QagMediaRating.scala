package com.appodealx.openrtb

import enumeratum.values.{IntEnum, IntEnumEntry}


sealed abstract class QagMediaRating(override val value: Int) extends IntEnumEntry with Serializable


object QagMediaRating extends IntEnum[QagMediaRating] {

  object AllAudiences extends QagMediaRating(1)

  object EveryoneOver12 extends QagMediaRating(2)

  object MatureAudiences extends QagMediaRating(3)

  val values = findValues

}
