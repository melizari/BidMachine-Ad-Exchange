package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class ClickTracking(id: Option[String] = None, value: String)

object ClickTracking {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.read[String])(ClickTracking.apply _)

  implicit val xmlWriter = XmlWriter { v: ClickTracking =>
    <ClickTracking>
      { xml.PCData(v.value) }
    </ClickTracking> addAttrOpt v.id.map("id" -> _)
  }

}