package com.appodealx.exchange.settings.persistance.buyer.repos

import com.appodealx.exchange.common.models.auction.{AuctionType, Protocol}
import com.appodealx.exchange.common.utils.{TypeClassInst, TypeTag}


abstract class BidderRepoInst[F[_], A: TypeTag] extends TypeClassInst[A]  {

  def query(q: BidderRepo.Query[A], at: AuctionType, p: Option[Protocol]): F[List[BidderRepo.Match]]

}
