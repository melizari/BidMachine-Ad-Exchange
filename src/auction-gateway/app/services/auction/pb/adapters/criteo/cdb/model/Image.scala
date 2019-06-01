package services.auction.pb.adapters.criteo.cdb.model

import io.circe.derivation._


case class Image(url: String, height: Int, width: Int)

object Image {
  implicit val decoder = deriveDecoder[Image]
  implicit val encoder = deriveEncoder[Image]
}