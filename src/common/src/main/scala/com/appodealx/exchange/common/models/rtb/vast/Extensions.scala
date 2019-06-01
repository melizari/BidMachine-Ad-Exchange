package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._

case class Extensions(extensions: List[Extension])

object Extensions {

  implicit val xmlReader = (__ \ "Extension").read(strictReadSeq[Extension].atLeast(1)).map(Extensions.apply)

  implicit val xmlWriter = XmlWriter { v: Extensions =>
    <Extensions>
      {v.extensions.toXml}
    </Extensions>
  }

}