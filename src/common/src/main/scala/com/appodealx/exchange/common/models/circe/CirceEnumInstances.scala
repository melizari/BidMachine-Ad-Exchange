package com.appodealx.exchange.common.models.circe

import enumeratum.values.{IntEnum, IntEnumEntry, StringEnum, StringEnumEntry}
import enumeratum.{Enum, EnumEntry}
import io.circe.{Decoder, Encoder}

import scala.util.Try


trait CirceEnumInstances {

  implicit def enumEncoder[A <: EnumEntry]: Encoder[A] =
    Encoder.encodeString.contramap(_.entryName)

  implicit def enumDecoder[A <: EnumEntry](implicit enum: Enum[A]): Decoder[A] =
    Decoder.decodeString.emapTry(s => Try(enum.withName(s)))

  implicit def stringEnumEncoder[A <: StringEnumEntry]: Encoder[A] =
    Encoder.encodeString.contramap(_.value)

  implicit def stringEnumDecoder[A <: StringEnumEntry](implicit enum: StringEnum[A]): Decoder[A] =
    Decoder.decodeString.emapTry(s => Try(enum.withValue(s)))

  implicit def intEnumDecoder[A <: IntEnumEntry](implicit enum: IntEnum[A]): Decoder[A] =
    Decoder.decodeInt.emapTry(i => Try(enum.withValue(i)))

  implicit def intEnumEncoder[A <: IntEnumEntry](implicit enum: IntEnum[A]): Encoder[A] =
    Encoder.encodeInt.contramap(_.value)

}
