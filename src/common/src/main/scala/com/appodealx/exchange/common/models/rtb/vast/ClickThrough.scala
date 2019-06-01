package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class ClickThrough(id: Option[String] = None, value: String)

object ClickThrough {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.read[String])(ClickThrough.apply _)

  implicit val xmlWriter = XmlWriter { v: ClickThrough =>
    <ClickThrough>
      { xml.PCData(v.value) }
    </ClickThrough> addAttrOpt v.id.map("id" -> _)
  }

}