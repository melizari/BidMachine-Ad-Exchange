package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class InteractiveCreativeFile(`type`: Option[String] = None,
                                   apiFramework: Option[String] = None,
                                   value: Uri)

object InteractiveCreativeFile {

  implicit val xmlReader = (
    attribute[String]("type").optional ~
    attribute[String]("apiFramework").optional ~
    __.read[Uri])(InteractiveCreativeFile.apply _)

  implicit val xmlWriter = XmlWriter { v: InteractiveCreativeFile =>
    val attrs = v.`type`.map("type" -> _).toList ++
      v.apiFramework.map("apiFramework" -> _)

    <InteractiveCreativeFile>
      {xml.PCData(v.value.toString)}
    </InteractiveCreativeFile> addAttrs attrs
  }

}