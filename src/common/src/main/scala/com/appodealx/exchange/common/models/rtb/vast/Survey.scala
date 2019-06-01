package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Survey(`type`: Option[String] = None, value: String)

object Survey {

  implicit val xmlReader = (
    attribute[String]("type").optional ~
    __.read[String])(Survey.apply _)

  implicit val xmlWriter = XmlWriter { v: Survey =>
    <Survey>
      {v.value}
    </Survey> addAttrOpt v.`type`.map("type" -> _)
  }

}