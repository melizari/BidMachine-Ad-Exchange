package services.auction.rtb

import akka.util.ByteString
import com.appodealx.exchange.common.models.auction.Bidder
import com.appodealx.openrtb.BidRequest
import models.auction.AdapterResult

trait Adapter[F[_]] {
  def announce(bidder: Bidder, request: BidRequest, cachedRequest: ByteString): F[AdapterResult]
}
