package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class IconClickThrough(value: Uri)

object IconClickThrough {

  implicit val xmlReader = __.read[Uri].map(IconClickThrough.apply)

  implicit val xmlWriter = XmlWriter { v: IconClickThrough =>
    <IconClickThrough>
      {xml.PCData(v.value.toString)}
    </IconClickThrough>
  }

}