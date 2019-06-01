package models

import java.io.StringWriter

import akka.util.ByteString
import com.appodealx.exchange.common.models.circe.CirceRtbInstances
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.exchange.common.utils.xtract.XmlWriter
import com.appodealx.openrtb.native.response.Native
import io.circe.Json
import play.api.http.Writeable
import play.twirl.api.Xml
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

trait DefaultWriteables extends CirceRtbInstances {

  implicit def writeableOf_VAST(implicit w: Writeable[Xml], wr: XmlWriter[VAST]): Writeable[VAST] = {
    w.map { vast =>
      val writer = new StringWriter
      xml.XML.write(writer, wr.write(vast).head, "utf-8", xmlDecl = true, null)
      Xml(writer.toString)
    }
  }

  import io.circe.syntax.EncoderOps

  implicit def writeableOf_NAST(implicit w: Writeable[Json]): Writeable[Native] = {
    w.map(_.asJson)
  }

  implicit def writeableOf_GeneratedMessage[M <: GeneratedMessage with Message[M]](
    implicit c: GeneratedMessageCompanion[M]
  ): Writeable[M] =
    Writeable(m => ByteString(m.toByteArray), Some(s"""application/x-protobuf;messageType="${c.scalaDescriptor.fullName}""""))

}
