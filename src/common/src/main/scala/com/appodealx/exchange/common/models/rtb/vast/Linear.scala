package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Linear(skipoffset: Option[Int] = None,
                  duration: Option[Duration] = None,
                  mediaFiles: Option[MediaFiles] = None,
                  adParameters: Option[AdParameters] = None,
                  videoClicks: Option[VideoClicks] = None,
                  trackingEvents: Option[TrackingEvents] = None,
                  icons: Option[Icons] = None)

object Linear {

  implicit val xmlReader = (
    attribute[Int]("skipoffset").optional ~
      (__ \ "Duration").readOptional[Duration] ~
      (__ \ "MediaFiles").readOptional[MediaFiles] ~
      (__ \ "AdParameters").readOptional[AdParameters] ~
      (__ \ "VideoClicks").readOptional[VideoClicks] ~
      (__ \ "TrackingEvents").readOptional[TrackingEvents] ~
      (__ \ "Icons").readOptional[Icons])(Linear.apply _)

  implicit val xmlWriter = XmlWriter { v: Linear =>
    <Linear>
      {v.duration.toXml}
      {v.mediaFiles.toXml}
      {v.adParameters.toXml}
      {v.videoClicks.toXml}
      {v.trackingEvents.toXml}
      {v.icons.toXml}
    </Linear> addAttrOpt v.skipoffset.map("skipoffset" -> _.toString)
  }

}