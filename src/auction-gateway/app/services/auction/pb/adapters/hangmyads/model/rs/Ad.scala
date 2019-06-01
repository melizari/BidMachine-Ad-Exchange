package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class Ad(
  `ad_id`: Option[String],
  `type`: Option[String],
  payout: Payout,
  cpm: Cpm,
  html: Option[String],
  campaign: Campaign,
  creative: Creative,
  tracking: Tracking
)

private[hangmyads] object Ad {
  implicit val decoder = deriveDecoder[Ad]
}
