package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Creative(id: Option[String] = None,
                    adid: Option[String] = None,
                    sequence: Option[Int] = None,
                    apiFramework: Option[String] = None,
                    universalAdId: Option[UniversalAdId] = None, // required in
                    creativeExtensions: Option[CreativeExtensions] = None,
                    linear: Option[Linear] = None,
                    nonLinearAds: Option[NonLinearAds] = None,
                    companionAds: Option[CompanionAds] = None)

object Creative {
  private val adidReader = attribute[String]("adid").optional | attribute[String]("AdID").optional

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    adidReader ~
    attribute[Int]("sequence").optional ~
    attribute[String]("apiFramework").optional ~
    (__ \ "UniversalAdId").readOptional[UniversalAdId] ~
    (__ \ "CreativeExtensions").readOptional[CreativeExtensions] ~
    (__ \ "Linear").readOptional[Linear] ~
    (__ \ "NonLinearAds").readOptional[NonLinearAds] ~
    (__ \ "CompanionAds").readOptional[CompanionAds])(Creative.apply _)

  implicit val xmlWriter = XmlWriter { v: Creative =>
    val attrs = v.id.map("id" -> _).toList ++
      v.adid.map("adid" -> _) ++
      v.sequence.map("sequence" -> _.toString) ++
      v.apiFramework.map("apiFramework" -> _)

    <Creative>
      {v.universalAdId.toXml}
      {v.creativeExtensions.toXml}
      {v.linear.toXml}
      {v.nonLinearAds.toXml}
      {v.companionAds.toXml}
    </Creative> addAttrs attrs
  }


}