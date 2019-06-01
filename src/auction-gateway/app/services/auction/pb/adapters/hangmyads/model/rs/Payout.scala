package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class Payout(`type`: Option[String], amount: Option[Double], currency: Option[String])

private[hangmyads] object Payout {
  implicit val decoder = deriveDecoder[Payout]
}
