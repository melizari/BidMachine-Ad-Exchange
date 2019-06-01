package services.auction.pb.adapters.vungle

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


case class VungleExtensionObject(token: String, `placement_id`: String)

object VungleExtensionObject {
  implicit val vungleExtensionObjectDecoder = deriveDecoder[VungleExtensionObject]
  implicit val vungleExtensionObjectEncoder = deriveEncoder[VungleExtensionObject]

}
