package com.appodealx.exchange.common.db

import java.util.UUID

import com.github.tminglei.slickpg.PgArraySupport
import slick.jdbc.JdbcType

trait SeqSupport { driver: PgArraySupport =>
  trait SeqImplicits {
    implicit val simpleUUIDSeqTypeMapper: JdbcType[Seq[UUID]] = new SimpleArrayJdbcType[UUID]("uuid").to(_.toSeq)
    implicit val simpleStrSeqTypeMapper: JdbcType[Seq[String]] = new SimpleArrayJdbcType[String]("text").to(_.toSeq)
    implicit val simpleLongSeqTypeMapper: JdbcType[Seq[Long]] = new SimpleArrayJdbcType[Long]("int8").to(_.toSeq)
    implicit val simpleIntSeqTypeMapper: JdbcType[Seq[Int]] = new SimpleArrayJdbcType[Int]("int4").to(_.toSeq)
    implicit val simpleFloatSeqTypeMapper: JdbcType[Seq[Float]] = new SimpleArrayJdbcType[Float]("float4").to(_.toSeq)
    implicit val simpleDoubleSeqTypeMapper: JdbcType[Seq[Double]] = new SimpleArrayJdbcType[Double]("float8").to(_.toSeq)
    implicit val simpleBoolSeqTypeMapper: JdbcType[Seq[Boolean]] = new SimpleArrayJdbcType[Boolean]("bool").to(_.toSeq)
  }
}
