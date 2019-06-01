package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class CustomClick(id: Option[String] = None, value: String)

object CustomClick {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.read[String])(CustomClick.apply _)


  implicit val xmlWriter = XmlWriter { v: CustomClick =>
    <CustomClick>
      { xml.PCData(v.value) }
    </CustomClick> addAttrOpt v.id.map("id" -> _)
  }


}