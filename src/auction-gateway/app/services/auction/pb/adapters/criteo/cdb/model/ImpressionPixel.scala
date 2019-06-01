package services.auction.pb.adapters.criteo.cdb.model

import io.circe.derivation._


case class ImpressionPixel(url: String)

object ImpressionPixel {
  implicit val decoder = deriveDecoder[ImpressionPixel]
  implicit val encoder = deriveEncoder[ImpressionPixel]
}