package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class IconClicks(iconClickThrough: Option[IconClickThrough] = None,
                      iconClickTracking: List[IconClickTracking])


object IconClicks {

  implicit val xmlReader = (
    (__ \ "IconClickThrough").readOptional[IconClickThrough] ~
    (__ \ "IconClickTracking").read(strictReadSeq[IconClickTracking]))(IconClicks.apply _)

  implicit val xmlWriter = XmlWriter { v: IconClicks =>
    <IconClicks>
      {v.iconClickThrough.toXml}
    </IconClicks>
  }

}