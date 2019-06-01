package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract._

case class IFrameResource(value: Uri)

object IFrameResource {

  implicit val xmlReader = __.read[Uri].map(IFrameResource.apply)

  implicit val xmlWriter = XmlWriter { v: IFrameResource =>
    <IFrameResource>
      {xml.PCData(v.value.toString)}
    </IFrameResource>
  }

}