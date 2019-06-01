package services.auction.pb.adapters.criteo.s2s.model.rs
import io.circe.derivation.deriveDecoder
import io.circe.derivation.deriveEncoder

private[s2s] case class Seatbid(seat: String, bid: List[Bid])

private[s2s] object Seatbid {
  implicit val decoder = deriveDecoder[Seatbid]
  implicit val encoder = deriveEncoder[Seatbid]
}
