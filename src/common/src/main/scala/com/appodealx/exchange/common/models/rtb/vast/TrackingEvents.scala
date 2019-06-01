package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._

case class TrackingEvents(events: List[Tracking])

object TrackingEvents {

  implicit val xmlReader = (__ \ "Tracking").read(strictReadSeq[Tracking].atLeast(1)).map(TrackingEvents.apply)

  implicit val xmlWriter = XmlWriter { v: TrackingEvents =>
    <TrackingEvents>
      { v.events.toXml }
    </TrackingEvents>
  }

}