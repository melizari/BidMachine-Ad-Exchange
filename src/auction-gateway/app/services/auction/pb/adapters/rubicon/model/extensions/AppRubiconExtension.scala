package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder


case class AppRubiconAdditions(`site_id`: Option[Int] = None)

case class AppRubiconExtension(rp: AppRubiconAdditions)

object AppRubiconExtension {
  implicit val appRubiconAdditionsEnc: Encoder[AppRubiconAdditions] = deriveEncoder[AppRubiconAdditions]
  implicit val appRubiconExtensionEnc: Encoder[AppRubiconExtension] = deriveEncoder[AppRubiconExtension]
}

