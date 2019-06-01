package services.auction.pb.adapters.smaato.model

import io.circe.derivation.deriveDecoder


case class Richmedia(mediadata: Mediadata, impressiontrackers: List[String], clicktrackers: List[String])

object Richmedia {
  implicit val decoder = deriveDecoder[Richmedia]
}