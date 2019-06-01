package services.auction.pb.adapters.smaato.model

import io.circe.derivation.deriveDecoder


case class Img(url: String, w: Int, h: Int, ctaurl: String)

object Img {
  implicit val decoder = deriveDecoder[Img]
}