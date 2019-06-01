package com.appodealx.exchange.common.models.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import enumeratum.values.{IntEnum, IntEnumEntry, StringEnum, StringEnumEntry}
import enumeratum.{Enum, EnumEntry}

trait JsoniterEnumInstances {

  implicit def enumCodec[A <: EnumEntry](implicit enum: Enum[A]): JsonValueCodec[A] = new JsonValueCodec[A] {
    override def decodeValue(in: JsonReader, default: A) = enum.withName(in.readString(""))

    override def encodeValue(x: A, out: JsonWriter): Unit = out.writeVal(x.entryName)

    override def nullValue = enum.values.head
  }

  implicit def stringEnumCodec[A <: StringEnumEntry](implicit enum: StringEnum[A]) = new JsonValueCodec[A] {
    override def decodeValue(in: JsonReader, default: A) = enum.withValue(in.readString(""))

    override def encodeValue(x: A, out: JsonWriter): Unit = out.writeVal(x.value)

    override def nullValue = enum.values.head
  }

  implicit def intEnumCodec[A <: IntEnumEntry](implicit enum: IntEnum[A]) = new JsonValueCodec[A] {
    override def decodeValue(in: JsonReader, default: A) = enum.withValue(in.readInt())

    override def encodeValue(x: A, out: JsonWriter): Unit = out.writeVal(x.value)

    override def nullValue = enum.values.head
  }
}
