package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._


case class Verification(vendor: Option[String] = None,
                        javaScriptResources: List[JavaScriptResource],
                        flashResources: List[FlashResource],
                        viewableImpression: Option[ViewableImpression] = None)

object Verification {

  implicit val xmlReader = (
    attribute[String]("vendor").optional ~
    (__ \ "JavaScriptResource").read(strictReadSeq[JavaScriptResource]) ~
    (__ \ "FlashResource").read(strictReadSeq[FlashResource]) ~
    (__ \ "ViewableImpression").readOptional[ViewableImpression]
  )(Verification.apply _)

  implicit val xmlWriter = XmlWriter { v: Verification =>
    <Verification>
      { v.javaScriptResources.toXml }
      { v.flashResources.toXml }
      { v.viewableImpression.toXml }
    </Verification> addAttrOpt v.vendor.map("vendor" -> _)
  }

}