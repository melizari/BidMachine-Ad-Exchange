package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class CompanionClickThrough(value: String)

object CompanionClickThrough {

  implicit val xmlReader = __.read[String].map(CompanionClickThrough.apply)

  implicit val xmlWriter = XmlWriter { v: CompanionClickThrough =>
    <CompanionClickThrough>
      {v.value}
    </CompanionClickThrough>
  }

}
