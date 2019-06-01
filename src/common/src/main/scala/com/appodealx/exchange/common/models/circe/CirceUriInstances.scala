package com.appodealx.exchange.common.models.circe

import akka.http.scaladsl.model.Uri
import com.appodealx.exchange.common.models.{Uri => LemonUri}
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.encoding.PercentEncoder
import io.lemonlabs.uri.encoding.PercentEncoder.QUERY_CHARS_TO_ENCODE


trait CirceUriInstances {
  import com.appodealx.exchange.common.utils.UriConversions

  implicit val uriEncoder: Encoder[Uri] with Decoder[Uri] =
    new Encoder[Uri] with Decoder[Uri] {
      override def apply(uri: Uri): Json = Encoder.encodeString.apply(uri.toString)

      override def apply(c: HCursor): Result[Uri] = Decoder.decodeString.map(_.toUri).apply(c)
    }

  implicit val lemonUriEncoder: Encoder[LemonUri] with Decoder[LemonUri] = {

    implicit val uriConfig = UriConfig.default.copy(queryEncoder = PercentEncoder(QUERY_CHARS_TO_ENCODE + '$'))

    new Encoder[LemonUri] with Decoder[LemonUri] {
      override def apply(uri: LemonUri): Json = Encoder.encodeString.apply(uri.toString)

      override def apply(c: HCursor): Result[LemonUri] = Decoder.decodeString.map(_.toLemonUri).apply(c)
    }
  }
}
