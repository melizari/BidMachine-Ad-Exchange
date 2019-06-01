package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class ViewableImpression(id: Option[String] = None,
                              uri: Option[Uri] = None,
                              viewable: List[Viewable],
                              notViewable: List[NotViewable],
                              viewUndetermined: List[ViewUndetermined])

object ViewableImpression {

  implicit val xmlReader = (
    attribute[String]("id").optional ~
    __.readOptional[Uri] ~
    (__ \ "Viewable").read(strictReadSeq[Viewable]) ~
    (__ \ "NotViewable").read(strictReadSeq[NotViewable]) ~
    (__ \ "ViewUndetermined").read(strictReadSeq[ViewUndetermined])
  )(ViewableImpression.apply _)

  implicit val xmlWriter = XmlWriter { v: ViewableImpression =>
    <ViewableImpression>
      { if (v.uri.isDefined) v.uri }
      { v.viewable.toXml }
      { v.notViewable.toXml }
      { v.viewUndetermined.toXml }
    </ViewableImpression> addAttrOpt v.id.map("id" -> _)
  }

}