package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class Advertiser(value: String)

object Advertiser {

  implicit val xmlReader = __.read[String].map(Advertiser.apply)

  implicit val xmlWriter = XmlWriter { v: Advertiser =>
    <Advertiser>
      {v.value}
    </Advertiser>
  }

}