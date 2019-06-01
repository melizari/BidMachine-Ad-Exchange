package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.{XmlWriter, __}

case class NotViewable(value: Uri)

object NotViewable {

  implicit val xmlReader = __.read[Uri].map(NotViewable.apply)

  implicit val xmlWriter = XmlWriter { v: NotViewable =>
    <NotViewable>
      {xml.PCData(v.value.toString)}
    </NotViewable>
  }

}