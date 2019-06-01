package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class NonLinear(staticResources: List[StaticResource],
                     iframeResources: List[IFrameResource],
                     htmlResources: List[HTMLResource],
                     adParameters: Option[AdParameters] = None,
                     nonLinearClickThrough: Option[NonLinearClickThrough] = None,
                     nonLinearClickTracking: List[NonLinearClickTracking])

object NonLinear {

  implicit val xmlReader = (
    (__ \ "StaticResource").read(strictReadSeq[StaticResource]) ~
    (__ \ "IFrameResource").read(strictReadSeq[IFrameResource]) ~
    (__ \ "HTMLResource").read(strictReadSeq[HTMLResource]) ~
    (__ \ "AdParameters").readOptional[AdParameters] ~
    (__ \ "NonLinearClickThrough").readOptional[NonLinearClickThrough] ~
    (__ \ "nonLinearClickTracking").read(strictReadSeq[NonLinearClickTracking]))(NonLinear.apply _)

  implicit val xmlWriter = XmlWriter { v: NonLinear =>
    <NonLinear>
      {v.staticResources.toXml}
      {v.iframeResources.toXml}
      {v.htmlResources.toXml}
      {v.adParameters.toXml}
      {v.nonLinearClickThrough.toXml}
      {v.nonLinearClickTracking.toXml}
    </NonLinear>
  }
}