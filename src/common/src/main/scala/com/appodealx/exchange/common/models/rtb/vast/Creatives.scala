package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._

case class Creatives(creatives: List[Creative])

object Creatives {

  implicit val xmlReader = (__ \ "Creative").read(strictReadSeq[Creative].atLeast(1)).map(Creatives.apply)

  implicit val xmlWriter = XmlWriter { v: Creatives =>
    <Creatives>
      {v.creatives.toXml}
    </Creatives>
  }

}