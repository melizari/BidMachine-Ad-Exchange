package com.appodealx.exchange.common.utils.circe

import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer.{NegotiatedDeserializer, NegotiatedSerializer}
import com.lightbend.lagom.scaladsl.api.deser.StrictMessageSerializer
import com.lightbend.lagom.scaladsl.api.transport.{DeserializationException, MessageProtocol, SerializationException, UnsupportedMediaType}
import io.circe._
import io.circe.parser._

import scala.collection.immutable.Seq
import scala.util.control.NonFatal

object CirceLagomMessageSerializer {

  trait CirceSerializable

  implicit def toSerializer[A](implicit encoder: Encoder[A], decoder: Decoder[A], printer: Printer = Printer.noSpaces.copy(dropNullValues = true)): StrictMessageSerializer[A] = new StrictMessageSerializer[A] {

    private val defaultProtocol = MessageProtocol(Some("application/json"), None, None)
    override val acceptResponseProtocols = List(defaultProtocol)

    private class JsValueSerializer(override val protocol: MessageProtocol) extends NegotiatedSerializer[A, ByteString] {
      override def serialize(message: A): ByteString = {
        try {
          ByteString.fromString(encoder(message).pretty(printer), protocol.charset.getOrElse("utf-8"))
        } catch {
          case NonFatal(e) => throw SerializationException(e)
        }
      }
    }

    private class JsValueDeserializer(val protocol: MessageProtocol) extends NegotiatedDeserializer[A, ByteString] {
      override def deserialize(wire: ByteString): A = {
        if (protocol.contentType.contains("application/json")) {
          try {
            val value = wire.decodeString(protocol.charset.getOrElse("utf-8"))
            decode(value) match {
              case Left(failure) => throw DeserializationException(failure)
              case Right(data) => data
            }
          } catch {
            case NonFatal(e) => throw DeserializationException(e)
          }
        } else {
          throw UnsupportedMediaType(protocol, defaultProtocol)
        }

      }

    }

    override def serializerForRequest: NegotiatedSerializer[A, ByteString] = new JsValueSerializer(defaultProtocol)

    override def serializerForResponse(acceptedMessageProtocols: Seq[MessageProtocol]): NegotiatedSerializer[A, ByteString] =
      new JsValueSerializer(acceptedMessageProtocols.find(_.contentType.contains("application/json")).getOrElse(defaultProtocol))

    override def deserializer(protocol: MessageProtocol): NegotiatedDeserializer[A, ByteString] = new JsValueDeserializer(protocol)

  }

}