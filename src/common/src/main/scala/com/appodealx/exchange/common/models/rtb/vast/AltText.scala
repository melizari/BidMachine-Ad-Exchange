package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class AltText(value: String)

object AltText {

  implicit val xmlReader = __.read[String].map(AltText.apply)

  implicit val xmlWriter = XmlWriter { v: AltText =>
    <AltText>
      {v.value}
    </AltText>
  }

}