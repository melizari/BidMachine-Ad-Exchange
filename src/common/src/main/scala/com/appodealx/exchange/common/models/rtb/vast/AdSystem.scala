package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class AdSystem(version: Option[String] = None, value: String)

object  AdSystem {

  implicit val xmlReader = (
    attribute[String]("version").optional ~
    __.read[String])(AdSystem.apply _)

  implicit val xmlWriter = XmlWriter { v: AdSystem =>
    <AdSystem>
      {v.value}
    </AdSystem> addAttrOpt v.version.map("version" -> _)
  }

}