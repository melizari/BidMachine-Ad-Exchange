package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class StaticResource(creativeType: String, value: Uri)

object StaticResource {

  implicit val xmlReader = (
    attribute[String]("creativeType") ~
    __.read[Uri])(StaticResource.apply _)

  implicit val xmlWriter = XmlWriter { v: StaticResource =>
    <StaticResource creativeType={v.creativeType}>
      {xml.PCData(v.value.toString)}
    </StaticResource>
  }

}