package services.auction.pb.adapters.rubicon.model

import com.appodealx.openrtb.Json

case class RubiconSeatBid(bid: List[RubiconBid],
                          buyer: Option[String],
                          seat: Option[String],
                          ext: Option[Json])
