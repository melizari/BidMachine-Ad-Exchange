package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._


case class FlashResource(apiFramework: Option[String] = None, value: Uri)

object FlashResource {

  implicit val xmlReader = (
    attribute[String]("apiFramework").optional ~
    __.read[Uri])(FlashResource.apply _)

    implicit val xmlWriter = XmlWriter { v: FlashResource =>
      <FlashResource>
        {xml.PCData(v.value.toString)}
      </FlashResource> addAttrOpt v.apiFramework.map("apiFramework" -> _)
    }


}