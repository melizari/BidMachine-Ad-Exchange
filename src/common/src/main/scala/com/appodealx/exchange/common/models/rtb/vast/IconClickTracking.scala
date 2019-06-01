package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class IconClickTracking(id: Option[String] = None, value: Uri)

object IconClickTracking {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.read[Uri])(IconClickTracking.apply _)

  implicit val xmlWriter = XmlWriter { v: IconClickTracking =>
    <IconClickTracking>
      {xml.PCData(v.value.toString)}
    </IconClickTracking> addAttrOpt v.id.map("id" -> _)
  }

}
