package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._
import org.joda.time.{Duration => JodaDuration}

case class Duration(value: JodaDuration)

object Duration {

  implicit val xmlReader = __.read[JodaDuration].map(Duration.apply)

  implicit val xmlWriter = XmlWriter { v: Duration =>
    <Duration>
      {v.value.toFormattedString}
    </Duration>
  }
}