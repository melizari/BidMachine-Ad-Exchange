package com.appodealx.exchange.common.utils.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriter
import io.circe.{Json, JsonNumber, JsonObject}

// Beware impure folder!
class CirceJsoniterFolder(writer: JsonWriter) extends Json.Folder[Unit] {
  override def onNull: Unit = writer.writeNull()

  override def onBoolean(value: Boolean): Unit = writer.writeVal(value)

  override def onNumber(value: JsonNumber): Unit = writer.writeVal(value.toBigDecimal.get)

  override def onString(value: String): Unit = writer.writeVal(value)

  override def onArray(value: Vector[Json]): Unit = {
    writer.writeArrayStart()

    if (value.isEmpty) {
      writer.writeArrayEnd()
    } else {
      val iterator = value.iterator

      while (iterator.hasNext) {
        writer.writeComma()
        iterator.next().foldWith(this)
      }

      writer.writeArrayEnd()
    }
  }

  override def onObject(value: JsonObject): Unit = {

    def writeKeyVal(t: (String, Json)): Unit = {
      writer.writeKey(t._1)
      t._2.foldWith(this)
    }

    writer.writeObjectStart()

    if (value.isEmpty) {
      writer.writeObjectEnd()
    } else {
      val iterator = value.toIterable.iterator

      while (iterator.hasNext) {
        writeKeyVal(iterator.next())
      }

      writer.writeObjectEnd()
    }
  }
}

object CirceJsoniterFolder {
  def apply(writer: JsonWriter) = new CirceJsoniterFolder(writer)
}
