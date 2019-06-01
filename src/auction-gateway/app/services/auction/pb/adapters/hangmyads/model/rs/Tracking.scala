package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class Tracking(impression: Option[String], click: String)

private[hangmyads] object Tracking {
  implicit val decoder = deriveDecoder[Tracking]
}
