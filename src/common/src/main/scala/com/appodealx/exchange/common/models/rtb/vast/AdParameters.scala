package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class AdParameters(xmlEncoded: Option[Boolean] = None, value: String)

object AdParameters {

  implicit val xmlReader = (
    attribute[Boolean]("xmlEncoded").optional ~
    __.read[String])(AdParameters.apply _)

  implicit val xmlWriter = XmlWriter { v: AdParameters =>
    <AdParameters>
      {v.value}
    </AdParameters> addAttrOpt v.xmlEncoded.map("xmlEncoded" -> _.toString)
  }

}