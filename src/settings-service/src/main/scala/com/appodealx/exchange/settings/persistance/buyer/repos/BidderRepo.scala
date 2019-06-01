package com.appodealx.exchange.settings.persistance.buyer.repos

import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.auction._
import com.appodealx.exchange.common.utils.{TypeClassSelector, TypeTag}
import com.github.zafarkhaja.semver.Version


object BidderRepo {

  case class Query[P](ad: P,
                      active: Boolean = true,
                      debug: Boolean = false,
                      adChannel: Option[Int] = None,
                      interstitial: Boolean = false,
                      reward: Boolean,
                      dmVer: Option[Version] = None,
                      coppa: Option[Boolean] = None,
                      platforms: Option[List[Platform]] = None,
                      countries: Option[List[String]] = None,
                      sellerId: Option[Long] = None)

  case class Match(agency: Agency, bidder: Bidder, profile: AdProfile)

}

abstract class BidderRepo[F[_]](repos: BidderRepoInst[F, _]*) extends TypeClassSelector[BidderRepoInst[F, ?]](repos) {

  def query[P: Plc](q: BidderRepo.Query[P], at: AuctionType, p: Option[Protocol] = None): F[List[BidderRepo.Match]] =
    selectInst[P].query(q, at, p)
}
