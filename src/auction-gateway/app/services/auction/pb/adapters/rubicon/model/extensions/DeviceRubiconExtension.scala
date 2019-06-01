package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder


case class DeviceRubiconAdditions(xff: Option[String] = None,
                                  res: Option[String] = None,
                                  pixelratio: Option[Float] = None
                                 )

case class DeviceRubiconExtension(rp: DeviceRubiconAdditions)

object DeviceRubiconExtension {
  implicit val deviceRubiconAdditionsEnc: Encoder[DeviceRubiconAdditions] = deriveEncoder[DeviceRubiconAdditions]
  implicit val deviceRubiconExtensionEnc: Encoder[DeviceRubiconExtension] = deriveEncoder[DeviceRubiconExtension]
}


