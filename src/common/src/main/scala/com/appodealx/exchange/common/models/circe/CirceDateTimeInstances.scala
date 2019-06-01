package com.appodealx.exchange.common.models.circe

import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.joda.time.{DateTime, Interval}

trait CirceDateTimeInstances {

  implicit val dateTimeEncoder: Encoder[DateTime] with Decoder[DateTime] =
    new Encoder[DateTime] with Decoder[DateTime] {

      override def apply(a: DateTime): Json = Encoder.encodeString.apply(a.toString)

      override def apply(c: HCursor): Result[DateTime] =
        Decoder.decodeString.map(s => DateTime.parse(s/*, ISODateTimeFormat.dateTime*/)).apply(c)
    }

  implicit val IntervalEncoder: Encoder[Interval] with Decoder[Interval] =
    new Encoder[Interval] with Decoder[Interval] {

      override def apply(a: Interval): Json = Encoder.encodeString.apply(a.toString)

      override def apply(c: HCursor): Result[Interval] =
        Decoder.decodeString.map(i => Interval.parse(i)).apply(c)
    }

}
