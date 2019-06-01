package services.auction.pb.adapters.smaato.model

import io.circe.derivation.deriveDecoder


case class Mediadata(content: String, w: Int, h: Int)

object Mediadata {
  implicit val decoder = deriveDecoder[Mediadata]
}