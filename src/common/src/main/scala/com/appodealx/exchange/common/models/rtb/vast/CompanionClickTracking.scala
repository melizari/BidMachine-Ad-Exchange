package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class CompanionClickTracking(id: Option[String] = None, value: Uri)

object CompanionClickTracking {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.read[Uri])(CompanionClickTracking.apply _)

  implicit val xmlWriter = XmlWriter { v: CompanionClickTracking =>
    <CompanionClickTracking>
      {xml.PCData(v.value.toString)}
    </CompanionClickTracking> addAttrOpt v.id.map("id" -> _)
  }

}