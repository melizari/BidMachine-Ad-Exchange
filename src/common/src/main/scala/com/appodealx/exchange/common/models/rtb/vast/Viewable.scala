package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.{XmlWriter, __}

case class Viewable(value: Uri)

object Viewable {

  implicit val xmlReader = __.read[Uri].map(Viewable.apply)

  implicit val xmlWriter = XmlWriter { v: Viewable =>
    <Viewable>
      {xml.PCData(v.value.toString)}
    </Viewable>
  }

}