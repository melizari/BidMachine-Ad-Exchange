package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class VideoClicks(clickThrough: Option[ClickThrough] = None,
                       clickTracking: List[ClickTracking],
                       customClick: List[CustomClick])

object VideoClicks {

  implicit val xmlReader = (
    (__ \ "ClickThrough").readOptional[ClickThrough] ~
    (__ \ "ClickTracking").read(strictReadSeq[ClickTracking]) ~
    (__ \ "CustomClick").read(strictReadSeq[CustomClick])
  )(VideoClicks.apply _)

  implicit val xmlWriter = XmlWriter { v: VideoClicks =>
    <VideoClicks>
      { v.clickThrough.toXml }
      { v.clickTracking.toXml }
      { v.customClick.toXml }
    </VideoClicks>
  }

}