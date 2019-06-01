package services.auction.rtb.filters

import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import models.auction.AdRequest
import services.auction.pb.adapters.pubmatic.PubmaticSettings

class PubmaticFilter(settings: PubmaticSettings) extends AdRequestFilter {
  override def filter[P: Plc](req: AdRequest[P], meta: Match) = settings.enabled(req)
}
