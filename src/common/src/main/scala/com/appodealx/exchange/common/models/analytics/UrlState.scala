package com.appodealx.exchange.common.models.analytics

import cats.syntax.either._
import io.circe._
import io.circe.parser._
import io.circe.syntax._


trait UrlStateEncoder[A] extends (A => String)

object UrlStateEncoder {
  import com.appodealx.exchange.common.utils.StringCoder

  implicit def circeJsonEncoder[A](implicit d: Encoder[A]): UrlStateEncoder[A] =
    (v1: A) => v1.asJson.pretty(Printer.noSpaces.copy(dropNullValues = true)).toBase64

}


trait UrlStateDecoder[A] extends (String => Either[String, A])

object UrlStateDecoder {
  import com.appodealx.exchange.common.utils.StringCoder

  //@annotation.implicitNotFound("implicit circe decoder not found")
  implicit def circeJsonDecoder[A](implicit d: Decoder[A]): UrlStateDecoder[A] =
    (v1: String) => decode(v1.fromBase64).leftMap(_.getMessage)

}