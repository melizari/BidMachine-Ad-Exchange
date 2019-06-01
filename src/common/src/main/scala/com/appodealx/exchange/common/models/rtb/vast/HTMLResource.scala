package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class HTMLResource(value: String)

object HTMLResource {

  implicit val xmlReader = __.read[String].map(HTMLResource.apply)

  implicit val xmlWriter = XmlWriter { v: HTMLResource =>
    <HTMLResource>
      {xml.PCData(v.value.toString)}
    </HTMLResource>
  }

}