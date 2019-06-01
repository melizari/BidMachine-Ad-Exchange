package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter
import play.api.libs.functional.syntax._

case class UniversalAdId(idRegistry: String,
                         idValue: String)

object UniversalAdId {

  implicit val xmlReader = (
    attribute[String]("idRegistry") ~
    attribute[String]("idValue"))(UniversalAdId.apply _)

  implicit val xmlWriter = XmlWriter { v: UniversalAdId =>
    <UniversalAdId idRegistry={v.idRegistry} idValue={v.idValue}></UniversalAdId>
  }

}