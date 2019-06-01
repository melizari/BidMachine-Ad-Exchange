package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.{XmlWriter, __}

case class ViewUndetermined(value: Uri)

object ViewUndetermined {

  implicit val xmlReader = __.read[Uri].map(ViewUndetermined.apply)

  implicit val xmlWriter = XmlWriter { v: ViewUndetermined =>
    <ViewUndetermined>
      { xml.PCData(v.value.toString) }
    </ViewUndetermined>
  }

}