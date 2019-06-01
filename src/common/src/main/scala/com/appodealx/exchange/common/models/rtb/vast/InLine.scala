package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class InLine(adSystem: Option[AdSystem],
                  adTitle: Option[AdTitle],
                  impression: List[Impression],
                  category: List[Category],
                  description: Option[Description] = None,
                  advertiser: Option[Advertiser] = None,
                  pricing: Option[Pricing] = None,
                  survey: List[Survey],
                  error: List[Error],
                  viewableImpression: Option[ViewableImpression] = None,
                  adVerifications: Option[AdVerifications] = None,
                  extensions: Option[Extensions] = None,
                  creatives: Creatives)

object InLine {

  implicit val xmlReader = (
    (__ \ "AdSystem").readOptional[AdSystem] ~
    (__ \ "AdTitle").readOptional[AdTitle] ~
    (__ \ "Impression").read(strictReadSeq[Impression]) ~
    (__ \ "Category").read(strictReadSeq[Category]) ~
    (__ \ "Description").readOptional[Description] ~
    (__ \ "Advertiser").readOptional[Advertiser] ~
    (__ \ "Pricing").readOptional[Pricing] ~
    (__ \ "Survey").read(strictReadSeq[Survey]) ~
    (__ \ "Error").read(strictReadSeq[Error]) ~
    (__ \ "ViewableImpression").readOptional[ViewableImpression] ~
    (__ \ "AdVerifications").readOptional[AdVerifications] ~
    (__ \ "Extensions").readOptional[Extensions] ~
    (__ \ "Creatives").read[Creatives])(InLine.apply _)

  implicit val xmlWriter = XmlWriter { v: InLine =>
    <InLine>
      {v.adSystem.toXml}
      {v.adTitle.toXml}
      {v.impression.toXml}
      {v.category.toXml}
      {v.description.toXml}
      {v.advertiser.toXml}
      {v.pricing.toXml}
      {v.survey.toXml}
      {v.error.toXml}
      {v.viewableImpression.toXml}
      {v.adVerifications.toXml}
      {v.extensions.toXml}
      {v.creatives.toXml}
    </InLine>
  }

}
