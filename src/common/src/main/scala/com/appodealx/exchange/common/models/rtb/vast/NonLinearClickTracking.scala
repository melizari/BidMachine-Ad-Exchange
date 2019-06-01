package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class NonLinearClickTracking(id: Option[String] = None, value: Uri)

object NonLinearClickTracking {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.read[Uri])(NonLinearClickTracking.apply _)

  implicit val xmlWriter = XmlWriter { v: NonLinearClickTracking =>
    <NonLinearClickTracking>
      {xml.PCData(v.value.toString)}
    </NonLinearClickTracking> addAttrOpt v.id.map("id" -> _)
  }

}