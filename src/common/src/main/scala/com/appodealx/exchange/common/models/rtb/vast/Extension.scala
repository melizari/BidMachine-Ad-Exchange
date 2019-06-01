package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._


case class Extension(`type`: Option[String] = None, value: xml.Node)

object Extension {

  implicit val xmlReader = (
    attribute[String]("type").optional ~
    __.read(nodeReader))(Extension.apply _)

  implicit val xmlWriter = XmlWriter { v: Extension =>
    <Extension>
      {v.value}
    </Extension> addAttrOpt v.`type`.map("type" -> _)
  }

}