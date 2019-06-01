package services.auction.pb.adapters.criteo.cdb.model

import io.circe.derivation.deriveEncoder


case class CriteoRequest(publisher: CriteoPublisher,
                         user: CriteoUser,
                         slots: List[SlotRequest])

object CriteoRequest {
  implicit val encoder = deriveEncoder[CriteoRequest]
}