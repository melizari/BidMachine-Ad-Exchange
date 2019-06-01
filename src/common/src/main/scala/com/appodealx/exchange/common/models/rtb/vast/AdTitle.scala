package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class AdTitle(value: String)

object AdTitle {

  implicit val xmlReader = __.read[String].map(AdTitle.apply)

  implicit val xmlWriter = XmlWriter { v: AdTitle =>
    <AdTitle>
      {v.value}
    </AdTitle>
  }

}
