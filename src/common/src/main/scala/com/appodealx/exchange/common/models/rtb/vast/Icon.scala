package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import org.joda.time.{Duration => JodaDuration}
import play.api.libs.functional.syntax._

case class Icon(program: Option[String] = None,
                width: Option[Int] = None,
                height: Option[Int] = None,
                xPosition: Option[Int] = None,
                yPosition: Option[Int] = None,
                duration: Option[JodaDuration] = None,
                offset: Option[JodaDuration] = None,
                apiFramework: Option[String] = None,
                pxratio: Option[Int] = None,
                staticResources: List[StaticResource],
                iframeResources: List[IFrameResource],
                htmlResources: List[HTMLResource],
                iconClicks: Option[IconClicks] = None,
                iconViewTrackings: List[IconViewTracking])

object Icon {

  implicit val xmlReader = (
    attribute[String]("program").optional ~
    attribute[Int]("width").optional ~
    attribute[Int]("height").optional ~
    attribute[Int]("xPosition").optional ~
    attribute[Int]("yPosition").optional ~
    attribute[JodaDuration]("duration").optional ~
    attribute[JodaDuration]("offset").optional ~
    attribute[String]("apiFramework").optional ~
    attribute[Int]("pxratio").optional ~
    (__ \ "StaticResource").read(strictReadSeq[StaticResource]) ~
    (__ \ "IFrameResource").read(strictReadSeq[IFrameResource]) ~
    (__ \ "HTMLResource").read(strictReadSeq[HTMLResource]) ~
    (__ \ "IconClicks").readOptional[IconClicks] ~
    (__ \ "IconViewTracking").read(strictReadSeq[IconViewTracking]))(Icon.apply _)

  implicit val xmlWriter = XmlWriter { v: Icon =>
    val attrs = v.program.map("program" -> _).toList ++
      v.width.map("width" -> _.toString) ++
      v.height.map("height" -> _.toString) ++
      v.xPosition.map("xPosition" -> _.toString) ++
      v.yPosition.map("yPosition" -> _.toString) ++
      v.duration.map("duration" -> _.toFormattedString) ++
      v.offset.map("offset" -> _.toFormattedString) ++
      v.apiFramework.map("apiFramework" -> _) ++
      v.pxratio.map("pxratio" -> _.toString)

    <Icon>
      {v.staticResources.toXml}
      {v.iframeResources.toXml}
      {v.htmlResources.toXml}
      {v.iconClicks.toXml}
      {v.iconViewTrackings.toXml}
    </Icon> addAttrs attrs
  }

}