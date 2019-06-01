package com.appodealx.exchange.common.db

import com.github.tminglei.slickpg.{PgArraySupport, PgCirceJsonSupport, utils}
import io.circe.parser.parse
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json, Printer}

import scala.reflect.ClassTag

trait JsonVectorSupport { driver: PgArraySupport with PgCirceJsonSupport =>

  private val p = Printer.noSpaces.copy(dropNullValues = true)

  implicit val jsonWitness = JsonWitness[Json]

  trait JsonVectorImplicits {
    implicit def circeArrayTypeMapper[A: ClassTag](implicit d: Decoder[A], e: Encoder[A], w: JsonWitness[A]) =
      new AdvancedArrayJdbcType[A](pgjson,
        s => utils.SimpleArrayUtils.fromString[A](parse(_).flatMap(_.as[A]).right.get)(s).orNull,
        v => utils.SimpleArrayUtils.mkString[A](_.asJson.pretty(p))(v)
      ).to(_.toVector)
  }
}
