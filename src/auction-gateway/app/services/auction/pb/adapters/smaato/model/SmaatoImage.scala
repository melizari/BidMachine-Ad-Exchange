package services.auction.pb.adapters.smaato.model

import io.circe.derivation.deriveDecoder

case class SmaatoImage(image: Image)

object SmaatoImage {
  implicit val decoder = deriveDecoder[SmaatoImage]
}