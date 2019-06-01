package com.appodealx.exchange.settings.persistance.buyer.repos

import com.appodealx.exchange.common.db.typeclasses.Execute
import scalacache.Mode

import cats.Functor

class BidderRepoImpl[F[_]: Execute: Functor: Mode](bannerInst: BannerBidderRepo[F],
                                                   videoInst: VideoBidderRepo[F],
                                                   nativeInst: NativeBidderRepo[F])
    extends BidderRepo(bannerInst, videoInst, nativeInst)
