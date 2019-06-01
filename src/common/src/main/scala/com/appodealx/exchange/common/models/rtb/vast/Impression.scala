package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Impression(id: Option[String] = None, value: Uri)

object Impression {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.read[Uri])(Impression.apply _)


  implicit val xmlWriter = XmlWriter { v: Impression =>
    <Impression>
      {xml.PCData(v.value.toString)}
    </Impression> addAttrOpt v.id.map("id" -> _)
  }

}
