package com.appodealx.openrtb

import enumeratum.values._


sealed abstract class Gender(override val value: String) extends StringEnumEntry with Serializable


object Gender extends StringEnum[Gender] {

  object Male extends Gender("M")

  object Female extends Gender("F")

  object Other extends Gender("O")

  val values = findValues

}

