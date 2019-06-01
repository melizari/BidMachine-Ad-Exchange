package services.auction.pb.adapters.smaato.model

import io.circe.derivation.deriveDecoder


case class Image(img: Img, impressiontrackers: List[String], clicktrackers: List[String])

object Image {
  implicit val decoder = deriveDecoder[Image]
}