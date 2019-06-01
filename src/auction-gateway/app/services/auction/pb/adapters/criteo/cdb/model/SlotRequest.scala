package services.auction.pb.adapters.criteo.cdb.model

import io.circe.ObjectEncoder
import io.circe.generic.semiauto.deriveEncoder


case class SlotRequest(impid: String,
                       zoneid: Int,
                       native: Option[Boolean] = None)

object SlotRequest {
  implicit val encoder: ObjectEncoder[SlotRequest] = deriveEncoder[SlotRequest]
}
