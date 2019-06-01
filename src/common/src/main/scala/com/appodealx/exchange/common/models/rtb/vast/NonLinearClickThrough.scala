package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class NonLinearClickThrough(value: Uri)

object NonLinearClickThrough {

  implicit val xmlReader = __.read[Uri].map(NonLinearClickThrough.apply)

  implicit val xmlWriter = XmlWriter { v: NonLinearClickThrough =>
    <NonLinearClickThrough>
      {xml.PCData(v.value.toString)}
    </NonLinearClickThrough>
  }

}