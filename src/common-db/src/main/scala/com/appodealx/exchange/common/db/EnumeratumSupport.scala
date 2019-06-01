package com.appodealx.exchange.common.db

import com.github.tminglei.slickpg.PgArraySupport
import enumeratum.values.{IntEnum, IntEnumEntry, StringEnum, StringEnumEntry}
import slick.jdbc.JdbcProfile

import scala.reflect.ClassTag

trait EnumeratumSupport { driver: JdbcProfile with PgArraySupport with SeqSupport =>
  import driver.api._

  trait StringEnumImplicits { self: SeqImplicits with ArrayImplicits=>
    implicit def stringEnumMapping[A <: StringEnumEntry : ClassTag](implicit enum: StringEnum[A]) = MappedColumnType.base[A, String](
      _.value,
      enum.withValue
    )

    implicit def stringEnumSeqMapping[A <: StringEnumEntry : ClassTag](implicit enum: StringEnum[A]) = MappedColumnType.base[Seq[A], Seq[String]](
      _.map(_.value),
      _.map(enum.withValue)
    )
  }

  trait IntEnumImplicits  { self: SeqImplicits with ArrayImplicits =>
    implicit def intEnumMapping[A <: IntEnumEntry : ClassTag](implicit enum: IntEnum[A]) = MappedColumnType.base[A, Int](
      _.value,
      enum.withValue
    )

    implicit def intEnumSeqMapping[A <: IntEnumEntry : ClassTag](implicit enum: IntEnum[A]) = MappedColumnType.base[Seq[A], Seq[Int]](
      _.map(_.value),
      _.map(enum.withValue)
    )

    implicit def intEnumListMapping[A <: IntEnumEntry : ClassTag](implicit enum: IntEnum[A]) = MappedColumnType.base[List[A], List[Int]](
      _.map(_.value),
      _.map(enum.withValue)
    )
  }
}
