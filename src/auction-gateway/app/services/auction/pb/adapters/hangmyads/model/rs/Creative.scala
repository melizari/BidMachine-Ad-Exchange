package services.auction.pb.adapters.hangmyads.model.rs

import io.circe.derivation.deriveDecoder

private[hangmyads] case class Creative(width: String, height: String, alt: Option[String], mime: Option[String], `media_file`: String)

private[hangmyads] object Creative {
  implicit val decoder = deriveDecoder[Creative]
}
