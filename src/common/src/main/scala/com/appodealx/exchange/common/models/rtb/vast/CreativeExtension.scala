package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class CreativeExtension(`type`: String, value: xml.Node)

object CreativeExtension {

  implicit val xmlReader = (
    attribute[String]("type") ~
    __.read(nodeReader))(CreativeExtension.apply _)


  implicit val xmlWriter = XmlWriter { v: CreativeExtension =>
    <CreativeExtension type={v.`type`}>
      {v.value}
    </CreativeExtension>
  }

}