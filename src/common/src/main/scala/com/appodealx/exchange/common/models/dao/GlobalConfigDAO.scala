package com.appodealx.exchange.common.models.dao

import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.db.{DBIOActionSyntax, GlobalConfigTable, PostgresProfile}
import com.appodealx.exchange.common.models.GlobalConfig
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig

import cats.Functor
import cats.syntax.functor._

trait GlobalConfigDAO[F[_]] {
  def find: F[Option[GlobalConfig]]
  def update(gc: GlobalConfig): F[GlobalConfig]
}

class GlobalConfigDAOImpl[
  F[_]: Execute: Functor
](protected val dbConfig: DatabaseConfig[PostgresProfile])
    extends GlobalConfigDAO[F]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax {

  import profile.api._

  def find: F[Option[GlobalConfig]] =
    GlobalConfigTable.result.headOption
      .lift[F]

  def update(gsp: GlobalConfig): F[GlobalConfig] =
    GlobalConfigTable
      .update(gsp)
      .lift[F]
      .map(_ => gsp)

}
