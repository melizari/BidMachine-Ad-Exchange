package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._

case class Icons(icons: List[Icon])

object Icons {

  implicit val xmlReader = (__ \ "Icon").read(strictReadSeq[Icon].atLeast(1)).map(Icons.apply)

  implicit val xmlWriter = XmlWriter { v: Icons =>
    <Icons>
      {v.icons.toXml}
    </Icons>
  }

}