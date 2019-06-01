package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class JavaScriptResource(apiFramework: Option[String] = None, value: Uri)

object JavaScriptResource {

  implicit val xmlReader = (
    attribute[String]("apiFramework").optional ~
    __.read[Uri])(JavaScriptResource.apply _)

  implicit val xmlWriter = XmlWriter { v: JavaScriptResource =>
    <JavaScriptResource>
      {xml.PCData(v.value.toString)}
    </JavaScriptResource> addAttrOpt v.apiFramework.map("apiFramework" -> _)
  }

}