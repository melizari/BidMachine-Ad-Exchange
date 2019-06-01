package services.auction.pb.adapters

import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import models.auction.{AdRequest, AdUnit, AdapterResult}

trait BidderCaller[F[_]] {

  def apply[P: Plc](adRequest: AdRequest[P], bidder: Bidder, adUnits: List[AdUnit]): F[AdapterResult]

}
