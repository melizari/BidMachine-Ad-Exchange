package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Category(authority: Option[String] = None, value: String)

object Category {

  implicit val xmlReader = (
    attribute[String]("authority").optional ~
    __.read[String])(Category.apply _)

  implicit val xmlWriter = XmlWriter { v: Category =>
    <Category>
      {v.value}
    </Category> addAttrOpt v.authority.map("authority" -> _)
  }

}