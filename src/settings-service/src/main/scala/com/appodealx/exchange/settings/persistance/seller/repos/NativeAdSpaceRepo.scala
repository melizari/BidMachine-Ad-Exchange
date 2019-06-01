package com.appodealx.exchange.settings.persistance.seller.repos

import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.appodealx.exchange.common.models.auction.Plc.NativePlc
import com.appodealx.exchange.common.models.dto.Native
import com.appodealx.exchange.settings.models.seller.{AdSpace, AdSpaceId, NativeAdSpace}
import com.appodealx.exchange.settings.persistance.seller.tables.NativeAdSpaces
import play.api.db.slick.HasDatabaseConfig
import scalacache.Mode
import scalacache.caffeine.CaffeineCache
import slick.basic.DatabaseConfig

import cats.ApplicativeError
import cats.syntax.option._

import scala.concurrent.duration._
import scala.language.postfixOps

class NativeAdSpaceRepo[F[_]: ApplicativeError[?[_], Throwable]: Execute: Mode](
  protected val dbConfig: DatabaseConfig[PostgresProfile],
  cache: CaffeineCache[NativeAdSpace]
) extends AdSpaceRepoInst[F, Native]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax {

  import profile.api._

  import cats.syntax.applicativeError._
  import cats.syntax.functor._

  private val ttl = 60 seconds

  private def slickQuery(id: Rep[AdSpaceId]) = NativeAdSpaces.filter(_.id === id)

  private val compiledQuery = Compiled(slickQuery _)

  def find(id: AdSpaceId) = {
    val key = s"ad-spaces/native/${id.value}"

    val action    = compiledQuery(id).result.headOption
    val fromDb    = action.lift[F].map(_.get)
    val fromCache = cache.cachingF(key)(ttl.some)(fromDb)

    fromCache.map(_.asInstanceOf[AdSpace[Native]].some).handleError(_ => None)
  }
}
