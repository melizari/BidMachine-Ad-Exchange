package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class GeoRubiconAdditions(consent: Option[Int] = Some(0))

case class GeoRubiconExtension(rp: GeoRubiconAdditions)

object GeoRubiconExtension {
  implicit val geoRubiconAdditionsEnc: Encoder[GeoRubiconAdditions] = deriveEncoder[GeoRubiconAdditions]
  implicit val geoRubiconExtensionEnc: Encoder[GeoRubiconExtension] = deriveEncoder[GeoRubiconExtension]
}


