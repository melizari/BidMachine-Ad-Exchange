package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._

case class AdVerifications(verifications: List[Verification])

object AdVerifications {

  implicit val xmlReader = (__ \ "Verification").read(strictReadSeq[Verification].atLeast(1)).map(AdVerifications.apply)

  implicit val xmlWriter = XmlWriter { v: AdVerifications =>
    <AdVerifications>
      {v.verifications.toXml}
    </AdVerifications>
  }

}