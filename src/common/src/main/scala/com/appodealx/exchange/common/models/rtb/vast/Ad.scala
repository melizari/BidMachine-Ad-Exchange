package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Ad(id: Option[String] = None,
              sequence: Option[Int] = None,
              conditionalAd: Option[Boolean] = None,
              inLine: Option[InLine] = None,
              wrapper: Option[Wrapper] = None)

object Ad {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    attribute[Int]("sequence").optional ~
    attribute[Boolean]("conditionalAd").optional ~
    (__ \ "InLine").readOptional[InLine] ~
    (__ \ "Wrapper").readOptional[Wrapper])(Ad.apply _)

  implicit val xmlWriter = XmlWriter { v: Ad =>
    val attrs = v.id.map("id" -> _).toList ++
      v.sequence.map("sequence" -> _.toString) ++
      v.conditionalAd.map("conditionalAd" -> _.toString)

    <Ad>
      {v.inLine.toXml}
      {v.wrapper.toXml}
    </Ad> addAttrs attrs
  }

}