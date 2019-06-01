package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class BidResponse(ads: Ads)

private[hangmyads] object BidResponse {
  implicit val decoder = deriveDecoder[BidResponse]
}
