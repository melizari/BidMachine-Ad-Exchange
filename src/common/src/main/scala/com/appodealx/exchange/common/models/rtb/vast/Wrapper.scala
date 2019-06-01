package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class Wrapper(followAdditionalWrappers: Option[Boolean] = None,
                   allowMultipleAds: Option[Boolean] = None,
                   fallbackOnNoAd: Option[Boolean] = None,
                   impression: List[Impression],
                   vastAdTagURI: VASTAdTagURI,
                   adSystem: Option[AdSystem] = None,
                   pricing: Option[Pricing] = None,
                   error: Option[Error] = None,
                   viewableImpression: Option[ViewableImpression] = None,
                   adVerifications: Option[AdVerifications] = None,
                   extensions: Option[Extensions] = None,
                   creatives: Option[Creatives] = None)

object Wrapper {

  implicit val xmlReader = (
    attribute[Boolean]("followAdditionalWrappers").optional ~
    attribute[Boolean]("allowMultipleAds").optional ~
    attribute[Boolean]("fallbackOnNoAd").optional ~
    (__ \ "Impression").read(strictReadSeq[Impression]) ~
    (__ \ "VASTAdTagURI").read[VASTAdTagURI] ~
    (__ \ "AdSystem").readOptional[AdSystem] ~
    (__ \ "Pricing").readOptional[Pricing] ~
    (__ \ "Error").readOptional[Error] ~
    (__ \ "ViewableImpression").readOptional[ViewableImpression] ~
    (__ \ "AdVerifications").readOptional[AdVerifications] ~
    (__ \ "Extensions").readOptional[Extensions] ~
    (__ \ "Creatives").readOptional[Creatives])(Wrapper.apply _)

  implicit val xmlWriter = XmlWriter { v: Wrapper =>

    val attrs = v.followAdditionalWrappers.map("followAdditionalWrappers" -> _.toString).toList ++
      v.allowMultipleAds.map("allowMultipleAds" -> _.toString) ++
      v.fallbackOnNoAd.map("fallbackOnNoAd" -> _.toString)


    <Wrapper>
      {v.impression.toXml}
      {v.vastAdTagURI.toXml}
      {v.adSystem.toXml}
      {v.pricing.toXml}
      {v.error.toXml}
      {v.viewableImpression.toXml}
      {v.adVerifications.toXml}
      {v.extensions.toXml}
      {v.creatives.toXml}
    </Wrapper> addAttrs attrs
  }

}