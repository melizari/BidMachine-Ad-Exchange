package services.auction.rtb.filters

import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import models.auction.AdRequest

trait AdRequestFilter {
  def filter[P: Plc](req: AdRequest[P], meta: Match): Boolean
}
