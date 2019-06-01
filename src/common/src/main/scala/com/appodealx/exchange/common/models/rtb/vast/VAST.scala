package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class VAST(ad: Option[List[Ad]] = None,
                error: Option[List[Error]] = None)

object VAST {

  implicit val xmlReader = (
    (__ \ "Ad").read(strictReadSeq[Ad].atLeast(1)).optional ~
    (__ \ "Error").read(strictReadSeq[Error].atLeast(1)).optional)(VAST.apply _)

  implicit val xmlWriter = XmlWriter { v: VAST =>
    <VAST>
      {v.ad.toXml}
      {v.error.toXml}
    </VAST>
  }

}