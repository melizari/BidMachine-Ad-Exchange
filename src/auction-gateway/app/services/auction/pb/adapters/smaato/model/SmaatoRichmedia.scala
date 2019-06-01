package services.auction.pb.adapters.smaato.model

import io.circe.derivation.deriveDecoder

case class SmaatoRichmedia(richmedia: Richmedia)

object SmaatoRichmedia {
  implicit val decoder = deriveDecoder[SmaatoRichmedia]
}
