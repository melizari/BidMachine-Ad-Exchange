package com.appodealx.exchange.common.db

import java.util.UUID

import com.github.tminglei.slickpg.PgArraySupport
import slick.jdbc.JdbcType

trait VectorSupport { driver: PgArraySupport =>
  trait VectorImplicits {
    implicit val simpleUUIDVectorTypeMapper: JdbcType[Vector[UUID]] = new SimpleArrayJdbcType[UUID]("uuid").to(_.toVector)
    implicit val simpleStrVectorTypeMapper: JdbcType[Vector[String]] = new SimpleArrayJdbcType[String]("text").to(_.toVector)
    implicit val simpleLongVectorTypeMapper: JdbcType[Vector[Long]] = new SimpleArrayJdbcType[Long]("int8").to(_.toVector)
    implicit val simpleIntVectorTypeMapper: JdbcType[Vector[Int]] = new SimpleArrayJdbcType[Int]("int4").to(_.toVector)
    implicit val simpleFloatVectorTypeMapper: JdbcType[Vector[Float]] = new SimpleArrayJdbcType[Float]("float4").to(_.toVector)
    implicit val simpleDoubleVectorTypeMapper: JdbcType[Vector[Double]] = new SimpleArrayJdbcType[Double]("float8").to(_.toVector)
    implicit val simpleBoolVectorTypeMapper: JdbcType[Vector[Boolean]] = new SimpleArrayJdbcType[Boolean]("bool").to(_.toVector)
  }
}
