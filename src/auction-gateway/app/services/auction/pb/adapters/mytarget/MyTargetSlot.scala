package services.auction.pb.adapters.mytarget

import io.circe.generic.semiauto.deriveDecoder


case class MyTargetSlot(`slot_id`: Long)

object MyTargetSlot {
  implicit val myTargetSlotDecoder = deriveDecoder[MyTargetSlot]
}
