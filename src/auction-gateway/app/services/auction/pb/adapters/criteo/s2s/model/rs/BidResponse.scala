package services.auction.pb.adapters.criteo.s2s.model.rs
import io.circe.derivation.deriveDecoder
import io.circe.derivation.deriveEncoder

private[s2s] case class BidResponse(requestid: String, seatbid: List[Seatbid])

private[s2s] object BidResponse {
  implicit val decoder = deriveDecoder[BidResponse]
  implicit val encoder = deriveEncoder[BidResponse]
}
