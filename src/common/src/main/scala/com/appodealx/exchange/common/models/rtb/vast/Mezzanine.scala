package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class Mezzanine(value: Uri)

object Mezzanine {

  implicit val xmlReader = __.read[Uri].map(Mezzanine.apply)

  implicit val xmlWriter = XmlWriter { v: Mezzanine =>
    <Mezzanine>
      {xml.PCData(v.value.toString)}
    </Mezzanine>
  }

}