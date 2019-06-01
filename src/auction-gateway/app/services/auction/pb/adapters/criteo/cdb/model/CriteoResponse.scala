package services.auction.pb.adapters.criteo.cdb.model

import io.circe.derivation.deriveDecoder


case class CriteoResponse(slots: List[CriteoSlot])

object CriteoResponse {
  implicit val decoder = deriveDecoder[CriteoResponse]
}