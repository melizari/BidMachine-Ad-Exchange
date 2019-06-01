package com.appodealx.exchange.common.models.rtb.vast

sealed abstract class PricingModel(override val entryName: String) extends VastEnumEntry

object PricingModel extends VastEnum[PricingModel] {

  object CPM extends PricingModel("CPM")
  object CPC extends PricingModel("CPC")
  object CPE extends PricingModel("CPE")
  object CPV extends PricingModel("CPV")

  val values = findValues
}