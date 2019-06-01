package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class Description(value: String)

object Description {

  implicit val xmlReader = __.read[String].map(Description.apply)

  implicit val xmlWriter = XmlWriter { v: Description =>
    <Description>
      {v.value}
    </Description>
  }

}