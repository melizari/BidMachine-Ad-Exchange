package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class Campaign(id: Option[String], app: Option[String], description: Option[String])

private[hangmyads] object Campaign {
  implicit val decoder = deriveDecoder[Campaign]
}
