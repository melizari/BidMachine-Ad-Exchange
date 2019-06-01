package com.appodealx.exchange.settings.persistance.buyer

import akka.http.scaladsl.model.Uri
import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.github.zafarkhaja.semver.Version
import enumeratum._
import slick.lifted.CanBeQueryCondition

import scala.language.higherKinds
import scala.reflect.ClassTag


package object tables {

  def enumMapping[A <: EnumEntry : ClassTag](enum: Enum[A]) = MappedColumnType.base[A, String](
    item => item.entryName,
    value => enum.withName(value)
  )

  def enumListMapping[A <: EnumEntry : ClassTag](enum: Enum[A]) = MappedColumnType.base[List[A], List[String]](
    _.map(_.entryName),
    _.map(enum.withName)
  )

  implicit class PimpedQuery[+E, U, C[_]](query: Query[E, U, C]) {

    def withOptionalFilter[A, T: CanBeQueryCondition](option: Option[A])(f: (E, A) => T): Query[E, U, C] = {
      if (option.isDefined) {
        query.withFilter(f(_, option.get))
      } else {
        query
      }
    }

  }

  implicit val uriMapping = MappedColumnType.base[Uri, String](_.toString, Uri.apply)
  implicit val versionMapping = MappedColumnType.base[Version, String](_.toString, Version.valueOf)
}
