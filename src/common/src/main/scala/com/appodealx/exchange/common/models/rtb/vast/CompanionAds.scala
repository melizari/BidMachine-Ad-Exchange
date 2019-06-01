package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class CompanionAds(required: Option[CompanionRequired] = None,
                        companions: List[Companion])

object CompanionAds {

  implicit val xmlReader = (
    attribute[CompanionRequired]("required").optional ~
    (__ \ "Companion").read(strictReadSeq[Companion].atLeast(1)))(CompanionAds.apply _)

  implicit val xmlWriter = XmlWriter { v: CompanionAds =>
    <CompanionAds>
      {v.companions.toXml}
    </CompanionAds> addAttrOpt v.required.map("required" -> _.entryName)
  }

}