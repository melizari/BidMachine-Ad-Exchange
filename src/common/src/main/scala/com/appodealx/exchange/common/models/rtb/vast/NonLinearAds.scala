package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class NonLinearAds(nonLinear: List[NonLinear], trackingEvents: Option[TrackingEvents] = None)

object NonLinearAds {

  implicit val xmlReader = (
    (__ \ "NonLinear").read(strictReadSeq[NonLinear]) ~
    (__ \ "TrackingEvents").readOptional[TrackingEvents])(NonLinearAds.apply _)

  implicit val xmlWriter = XmlWriter { v: NonLinearAds =>
    <NonLinearAds>
      {v.nonLinear.toXml}
      {v.trackingEvents.toXml}
    </NonLinearAds>
  }

}