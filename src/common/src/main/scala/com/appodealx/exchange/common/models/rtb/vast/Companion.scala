package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Companion(width: Int,
                     height: Int,
                     id: Option[String] = None,
                     assetWidth: Option[Int] = None,
                     assetHeight: Option[Int] = None,
                     expandedWidth: Option[Int] = None,
                     expandedHeight: Option[Int] = None,
                     apiFramework: Option[String] = None,
                     adslotID: Option[String] = None,
                     pxratio: Option[Int] = None,
                     staticResources: List[StaticResource],
                     iFrameResources: List[IFrameResource],
                     htmlResources: List[HTMLResource],
                     adParameters: Option[AdParameters] = None,
                     altText: Option[AltText] = None,
                     companionClickThrough: Option[CompanionClickThrough] = None,
                     companionClickTrackings: List[CompanionClickTracking],
                     trackingEvents: Option[TrackingEvents] = None)

object Companion {

  implicit val xmlReader = (
    attribute[Int]("width") ~
    attribute[Int]("height") ~
    attribute[String]("id").optional ~
    attribute[Int]("assetWidth").optional ~
    attribute[Int]("assetHeight").optional ~
    attribute[Int]("expandedWidth").optional ~
    attribute[Int]("expandedHeight").optional ~
    attribute[String]("apiFramework").optional ~
    attribute[String]("adslotID").optional ~
    attribute[Int]("pxratio").optional ~
    (__ \ "StaticResource").read(strictReadSeq[StaticResource]) ~
    (__ \ "IFrameResource").read(strictReadSeq[IFrameResource]) ~
    (__ \ "HTMLResource").read(strictReadSeq[HTMLResource]) ~
    (__ \ "AdParameters").readOptional[AdParameters] ~
    (__ \ "AltText").readOptional[AltText] ~
    (__ \ "CompanionClickThrough").readOptional[CompanionClickThrough] ~
    (__ \ "CompanionClickTracking").read(strictReadSeq[CompanionClickTracking]) ~
    (__ \ "TrackingEvents").readOptional[TrackingEvents])(Companion.apply _)

  implicit val xmlWriter = XmlWriter { v: Companion =>

    val attrs = v.assetWidth.map("assetWidth" -> _.toString).toList ++
      v.assetHeight.map("assetHeight" -> _.toString) ++
      v.expandedWidth.map("expandedWidth" -> _.toString) ++
      v.expandedHeight.map("expandedHeight" -> _.toString) ++
      v.apiFramework.map("apiFramework" -> _) ++
      v.adslotID.map("adslotID" -> _) ++
      v.pxratio.map("pxratio" -> _.toString)

    <Companion width={v.width.toString} height={v.height.toString}>
      {v.staticResources.toXml}
      {v.iFrameResources.toXml}
      {v.htmlResources.toXml}
      {v.adParameters.toXml}
      {v.altText.toXml}
      {v.companionClickThrough.toXml}
      {v.companionClickTrackings.toXml}
      {v.trackingEvents.toXml}
    </Companion> addAttrs attrs
  }

}