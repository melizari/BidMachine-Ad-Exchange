package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._
import enumeratum.{Enum, EnumEntry}

trait VastEnumEntry extends EnumEntry

trait VastEnum[A <: VastEnumEntry] extends Enum[A] { enum =>

  import Function._

  implicit val xmlReader = __.read[String].collect(unlift(enum.withNameOption))

}