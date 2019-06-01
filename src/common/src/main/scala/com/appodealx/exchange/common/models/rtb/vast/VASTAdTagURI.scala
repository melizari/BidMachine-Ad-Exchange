package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class VASTAdTagURI(value: Uri)

object VASTAdTagURI {

  implicit val xmlReader = __.read[Uri].map(VASTAdTagURI.apply)

  implicit val xmlWriter = XmlWriter { v: VASTAdTagURI =>
    <VASTAdTagURI>
      { xml.PCData(v.value.toString) }
    </VASTAdTagURI>
  }

}