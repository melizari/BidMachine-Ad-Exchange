package services.auction.pb.adapters.rubicon.model

import com.appodealx.openrtb.NoBidReason
import io.circe.Json

case class RubiconBidResponse(id: String,
                              seatbid: Option[List[RubiconSeatBid]] = None,
                              bidid: Option[String] = None,
                              statuscode: Option[Int] = None,
                              nbr: Option[NoBidReason] = None,
                              statusmsg: Option[String] = None,
                              ext: Option[Json] = None)