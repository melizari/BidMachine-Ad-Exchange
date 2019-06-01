package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class Error(value: Uri)

object Error {

  implicit val xmlReader = __.read[Uri].map(Error.apply)

  implicit val xmlWriter = XmlWriter { v: Error =>
    <Error>
      {xml.PCData(v.value.toString)}
    </Error>
  }

}