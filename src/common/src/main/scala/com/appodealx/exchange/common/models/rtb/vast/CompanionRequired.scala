package com.appodealx.exchange.common.models.rtb.vast

sealed abstract class CompanionRequired(override val entryName: String) extends VastEnumEntry

object CompanionRequired extends VastEnum[CompanionRequired] {

  object All extends CompanionRequired("all")
  object Any extends CompanionRequired("any")
  object None extends CompanionRequired("none")

  val values = findValues

}
