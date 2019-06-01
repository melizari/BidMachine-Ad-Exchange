package services.auction.rtb

import akka.util.ByteString
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import com.appodealx.openrtb.BidRequest

package object reqmodifiers {
  type BidRequestModifier = (Match, BidRequest) => (BidRequest, ByteString)
}
