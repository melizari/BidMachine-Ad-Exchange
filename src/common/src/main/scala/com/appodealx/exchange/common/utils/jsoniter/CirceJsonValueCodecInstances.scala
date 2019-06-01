package com.appodealx.exchange.common.utils.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import io.circe._

import scala.collection.mutable.ArrayBuffer

trait CirceJsonValueCodecInstances {

  implicit val circeJsonValueCodec: JsonValueCodec[Json] = new JsonValueCodec[Json] {
    val nullValue = Json.obj()

    def decodeValue(in: JsonReader, default: Json) = {
      val b = in.nextToken()
      in.rollbackToken()

      if (b == '"') {
        Json.fromString(in.readString(""))
      } else if ((b >= '0' && b <= '9') || b == '-') {
        Json.fromBigDecimal(in.readBigDecimal(0))
      } else if (b == 'f' || b == 't') {
        Json.fromBoolean(in.readBoolean())
      } else if (b == '[') {
        in.nextToken()
        if (in.isNextToken(']')) {
          Json.arr()
        } else {
          in.rollbackToken()
          val values = ArrayBuffer.empty[Json]
          do {
            values += decodeValue(in, default)
          } while (in.isNextToken(','))
          if (!in.isCurrentToken(']')) in.arrayEndError()
          Json.fromValues(values)
        }
      } else if (b == '{') {
        in.nextToken()
        if(in.isNextToken('}')) {
          Json.obj()
        } else {
          in.rollbackToken()
          val values = ArrayBuffer.empty[(String, Json)]
          do {
            val key = in.readKeyAsString()
            val value = decodeValue(in, default)
            values += key -> value
          } while (in.isNextToken(','))
          if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
          Json.fromFields(values)
        }
      } else if (in.isNextToken('n')) {
        in.readNullOrError(Json.Null, "null expected")
      } else {
        in.decodeError("valid JSON token expected")
      }
    }

    def encodeValue(x: Json, out: JsonWriter): Unit = x.foldWith(CirceJsoniterFolder(out))
  }
}
