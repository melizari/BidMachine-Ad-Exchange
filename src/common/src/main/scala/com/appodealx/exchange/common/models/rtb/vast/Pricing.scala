package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Pricing(model: PricingModel, currency: String, value: Double)

object Pricing {

  implicit val xmlReader = (
    attribute[PricingModel]("model") ~
    attribute[String]("currency") ~
    __.read[Double])(Pricing.apply _)

  implicit val xmlWriter = XmlWriter { v: Pricing =>
    <Pricing model={v.model.entryName} currency={v.currency}>
      {v.value.toString}
    </Pricing>
  }

}