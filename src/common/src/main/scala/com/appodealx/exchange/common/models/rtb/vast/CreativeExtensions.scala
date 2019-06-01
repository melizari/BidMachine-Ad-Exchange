package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._

case class CreativeExtensions(creativeExtension: List[CreativeExtension])

object CreativeExtensions {

  implicit val xmlReader = (__ \ "CreativeExtension").read(strictReadSeq[CreativeExtension].atLeast(1)).map(CreativeExtensions.apply)

  implicit val xmlWriter = XmlWriter { v: CreativeExtensions =>
    <CreativeExtensions>
      {v.creativeExtension.toXml}
    </CreativeExtensions>
  }

}