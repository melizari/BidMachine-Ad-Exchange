package com.appodealx.exchange.common.models.rtb.vast
import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract._

case class MoPubForceOrientation(value: String)

object MoPubForceOrientation {
  implicit val xmlReader = __.read[String].map(MoPubForceOrientation.apply)

  implicit val xmlWriter = XmlWriter { v: String =>
    <MoPubForceOrientation>
      {v}
    </MoPubForceOrientation>
  }

  def extension = Extension(`type` = Some("MoPub"), xmlWriter.write("Portrait").head)
}
