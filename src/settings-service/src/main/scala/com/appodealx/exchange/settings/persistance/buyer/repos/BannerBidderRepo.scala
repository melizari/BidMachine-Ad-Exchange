package com.appodealx.exchange.settings.persistance.buyer.repos

import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.appodealx.exchange.common.models.auction.{AuctionType, Protocol}
import com.appodealx.exchange.common.models.auction.Plc.BannerPlc
import com.appodealx.exchange.common.models.dto.Banner
import com.appodealx.exchange.settings.persistance.buyer.tables._
import play.api.Logger
import play.api.db.slick.HasDatabaseConfig
import scalacache.{CacheAlg, Mode}
import slick.basic.DatabaseConfig

import cats.Functor
import cats.syntax.functor._
import cats.syntax.option._

import scala.concurrent.duration._
import scala.language.postfixOps

class BannerBidderRepo[F[_]: Execute: Functor: Mode](
  cache: CacheAlg[List[BidderRepo.Match]],
  protected val dbConfig: DatabaseConfig[PostgresProfile]
) extends BidderRepoInst[F, Banner]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax
    with ListMappingInstances
    with EnumMappingInstances {

  import profile.api._

  private val ttl = 60 seconds

  private def cacheKey(q: BidderRepo.Query[Banner]) = {
    val w = q.ad.w.getOrElse(0)
    val h = q.ad.h.getOrElse(0)

    s"/v3/bidders/banner/$w-$h-${q.interstitial}-${q.active}-${q.debug}"
  }

  private def dbQuery(
    active: Rep[Boolean],
    debug: Rep[Boolean],
    inst: Rep[Boolean],
    w: Rep[Option[Int]],
    h: Rep[Option[Int]]
  ) = {

    val agencies = Agencies.filter(_.active === active)

    val profiles = BannerAdProfiles.filter { profile =>
      profile.active === active &&
      profile.debug === debug &&
      profile.interstitial === inst &&
      profile.rtbBannerW === w &&
      profile.rtbBannerH === h
    }

    for {
      p <- profiles
      b <- Bidders if b.id === p.bidderId
      a <- agencies if a.id === b.agencyId
    } yield (a, b, p)
  }

  private val compiledQuery = Compiled(dbQuery _)

  private def fetchDb(q: BidderRepo.Query[Banner]) =
    compiledQuery(q.active, q.debug, q.interstitial, q.ad.w, q.ad.h).result
      .lift[F]
      .map(_.map(BidderRepo.Match.tupled).toList)

  def filterResults(q: BidderRepo.Query[Banner], at: AuctionType, p: Option[Protocol])(
    result: BidderRepo.Match
  ): Boolean = {
    val countries     = q.countries.getOrElse(Nil)
    val countriesPred = result.bidder.worldwide || result.bidder.countries.getOrElse(Nil).containsSlice(countries)

    val platforms     = q.platforms.getOrElse(Nil)
    val platformsPred = result.bidder.platforms.containsSlice(platforms)

    val adChannelPred = q.adChannel.isEmpty || result.profile.adChannel.contains(q.adChannel.get)

    val dmVerMaxPred = result.profile.dmVerMax.isEmpty || q.dmVer.exists(
      _ lessThanOrEqualTo result.profile.dmVerMax.get
    )
    val dmVerMinPred = result.profile.dmVerMin.isEmpty || q.dmVer.exists(
      _ greaterThanOrEqualTo result.profile.dmVerMin.get
    )

    val coppaPred = !q.coppa.getOrElse(false) || result.bidder.coppaFlag

    val includedSellers = result.bidder.includedSellers.getOrElse(Seq.empty)
    val excludedSellers = result.bidder.excludedSellers.getOrElse(Seq.empty)

    val sellerNotExcluded = !q.sellerId.fold(false)(excludedSellers.contains(_))
    val sellerIncluded    = includedSellers.isEmpty || q.sellerId.fold(false)(includedSellers.contains(_))
    val sellerPred        = sellerNotExcluded && sellerIncluded

    val auctionTypePred = result.bidder.auctionType == at
    val rewardPred      = result.profile.reward == q.reward
    val protocol        = p.fold(true)(_ == result.bidder.protocol)

    Logger.debug(
      s"Checking Bidder ${result.bidder.title} AdProfile ${result.profile.title} (${result.profile.id}):" +
        s"GEO:$countriesPred|" +
        s"OS:$platformsPred|" +
        s"CH:$adChannelPred|" +
        s"DMX:$dmVerMaxPred|" +
        s"DMN:$dmVerMinPred|" +
        s"COP:$coppaPred|" +
        s"SEL:$sellerPred|" +
        s"AT:$auctionTypePred|" +
        s"RW:$rewardPred|" +
        s"PR:$protocol"
    )

    countriesPred && platformsPred && adChannelPred && dmVerMaxPred &&
    dmVerMinPred && coppaPred && sellerPred && auctionTypePred && rewardPred && protocol
  }

  def query(q: BidderRepo.Query[Banner], at: AuctionType, p: Option[Protocol]) = {
    val fromDb    = fetchDb(q)
    val fromCache = cache.cachingF(cacheKey(q))(ttl.some)(fromDb)
    val filter    = filterResults(q, at, p)(_)

    fromCache.map(_.filter(filter))
  }
}
