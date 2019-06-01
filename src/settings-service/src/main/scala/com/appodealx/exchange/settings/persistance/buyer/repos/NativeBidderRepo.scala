package com.appodealx.exchange.settings.persistance.buyer.repos

import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.appodealx.exchange.common.models.auction.{AuctionType, Protocol}
import com.appodealx.exchange.common.models.auction.Plc.NativePlc
import com.appodealx.exchange.common.models.dto.Native
import com.appodealx.exchange.settings.persistance.buyer.tables._
import play.api.db.slick.HasDatabaseConfig
import scalacache.{CacheAlg, Mode}
import slick.basic.DatabaseConfig

import cats.Functor
import cats.syntax.functor._
import cats.syntax.option._

import scala.concurrent.duration._
import scala.language.postfixOps

class NativeBidderRepo[F[_]: Execute: Functor: Mode](cache: CacheAlg[List[BidderRepo.Match]],
                                                     protected val dbConfig: DatabaseConfig[PostgresProfile])
    extends BidderRepoInst[F, Native]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax
    with ListMappingInstances
    with EnumMappingInstances {

  import profile.api._

  private val ttl = 60.seconds

  private def cacheKey(q: BidderRepo.Query[Native]) =
    s"/v3/bidders/native/${q.active}-${q.debug}"

  def dbQuery(active: Rep[Boolean], debug: Rep[Boolean]) = {
    val agencies = Agencies.filter(_.active === active)

    val profiles = NativeAdProfiles.filter(p => (p.active === active) && (p.debug === debug))

    for {
      p <- profiles
      b <- Bidders if b.id === p.bidderId
      a <- agencies if a.id === b.agencyId
    } yield (a, b, p)
  }

  private val compiledQuery = Compiled(dbQuery _)

  def fetchDb(q: BidderRepo.Query[Native]) =
    compiledQuery(q.active, q.debug).result
      .lift[F]
      .map(_.map(BidderRepo.Match.tupled).toList)

  private def filterResults(q: BidderRepo.Query[Native], at: AuctionType, p: Option[Protocol])(result: BidderRepo.Match): Boolean = {
    val countries     = q.countries.getOrElse(Nil)
    val countriesPred = result.bidder.worldwide || result.bidder.countries.getOrElse(Nil).containsSlice(countries)

    val platforms     = q.platforms.getOrElse(Nil)
    val platformsPred = result.bidder.platforms.containsSlice(platforms)

    val coppaPred = !q.coppa.getOrElse(false) || result.bidder.coppaFlag

    val includedSellers = result.bidder.includedSellers.getOrElse(Seq.empty)
    val excludedSellers = result.bidder.excludedSellers.getOrElse(Seq.empty)

    val sellerNotExcluded = !q.sellerId.fold(false)(excludedSellers.contains(_))
    val sellerIncluded    = includedSellers.isEmpty || q.sellerId.fold(false)(includedSellers.contains(_))
    val sellerPred        = sellerNotExcluded && sellerIncluded

    val auctionTypePred = result.bidder.auctionType == at
    val rewardPred      = result.profile.reward == q.reward
    val protocol        = p.fold(true)(_ == result.bidder.protocol)

    countriesPred && platformsPred && coppaPred && sellerPred && auctionTypePred && rewardPred && protocol
  }

  def query(q: BidderRepo.Query[Native], at: AuctionType, p: Option[Protocol]) = {
    val fromDb    = fetchDb(q)
    val fromCache = cache.cachingF(cacheKey(q))(ttl.some)(fromDb)
    val filter    = filterResults(q, at, p)(_)

    fromCache.map(_.filter(filter))
  }

}
