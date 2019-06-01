package com.appodealx.exchange.common.utils

import io.circe.{Json, JsonNumber, JsonObject}
import play.api.libs.json._


object json {

  implicit class PimpedJsValue(value: JsValue) {
    def toCirce: Json = value match {
      case JsNull => Json.Null
      case JsBoolean(b) => Json.fromBoolean(b)
      case JsString(s) => Json.fromString(s)
      case JsArray(a) => Json.arr(a.map(_.toCirce): _*)
      case JsObject(m) => Json.obj(m.mapValues(_.toCirce).toList: _*)
    }
  }

  trait PlayFolder[X] {
    def onNull: X

    def onBoolean(value: Boolean): X

    def onNumber(value: JsNumber): X

    def onString(value: String): X

    def onArray(value: Vector[JsValue]): X

    def onObject(value: JsObject): X
  }

  object PlayJsonFolder extends Json.Folder[JsValue] {
    override def onNull = JsNull
    override def onBoolean(value: Boolean) = JsBoolean(value)
    override def onNumber(value: JsonNumber) = JsNumber(value.toBigDecimal.get)
    override def onString(value: String) = JsString(value)
    override def onArray(value: Vector[Json]) = JsArray(value.map(_.foldWith(this)))
    override def onObject(value: JsonObject) = JsObject(value.toVector.map(t => t._1 -> t._2.foldWith(this)))
  }

  object CirceJsonFolder extends PlayFolder[Json] {
    override def onNull = Json.Null
    override def onBoolean(value: Boolean) = Json.fromBoolean(value)
    override def onNumber(value: JsNumber) = Json.fromBigDecimal(value.value)
    override def onString(value: String) = Json.fromString(value)
    override def onArray(value: Vector[JsValue]) = Json.arr(value.map(_.foldWith(this)): _*)
    override def onObject(value: JsObject) = Json.obj(value.fields.map(t => t._1 -> t._2.foldWith(this)): _*)
  }

  implicit class FoldableJsValue(value: JsValue) {
    def foldWith[X](folder: PlayFolder[X]): X = value match {
      case JsNull => folder.onNull
      case JsBoolean(b) => folder.onBoolean(b)
      case n: JsNumber => folder.onNumber(n)
      case JsString(s) => folder.onString(s)
      case JsArray(a) => folder.onArray(a.toVector)
      case o: JsObject => folder.onObject(o)
    }
  }

}
