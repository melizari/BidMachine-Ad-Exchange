package services.auction.pb.adapters.criteo.s2s.model.rq

import io.circe.derivation.deriveEncoder

private[s2s] case class Auction(id: Option[String], timeout: Option[Int], currency: Option[String])


private[s2s] object Auction {
  implicit val encoder = deriveEncoder[Auction]
}
