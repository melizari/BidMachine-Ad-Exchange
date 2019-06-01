package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class IconViewTracking(value: Uri)

object IconViewTracking {

  implicit val xmlReader = __.read[Uri].map(IconViewTracking.apply)

  implicit val xmlWriter = XmlWriter { v: IconViewTracking =>
    <IconViewTracking>
      {xml.PCData(v.value.toString)}
    </IconViewTracking>
  }

}