package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Tracking(event: String, offset: Option[String] = None, value: String)

object Tracking {

  implicit val xmlReader = (
    attribute[String]("event") ~
    attribute[String]("offset").optional ~
    __.read[String])(Tracking.apply _)

  implicit val xmlWriter = XmlWriter { v: Tracking =>
    <Tracking event={v.event}>
      {xml.PCData(v.value)}
    </Tracking> addAttrOpt v.offset.map("offset" -> _)
  }

}