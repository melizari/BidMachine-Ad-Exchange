package services.auction.pb.adapters.criteo.cdb.model

import io.circe.derivation._


case class Privacy(`optout_click_url`: String, `optout_image_url`: String)

object Privacy {
  implicit val decoder = deriveDecoder[Privacy]
  implicit val encoder = deriveEncoder[Privacy]
}