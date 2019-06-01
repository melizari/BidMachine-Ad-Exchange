package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class AppPublisherAdditions(`account_id`: Option[Int] = None)

case class AppPublisherExtension(rp: AppPublisherAdditions)

object AppPublisherExtension {
  implicit val appRubiconPublisherAdditionsEnc: Encoder[AppPublisherAdditions] = deriveEncoder[AppPublisherAdditions]
  implicit val appRubiconPublisherExtensionEnc: Encoder[AppPublisherExtension] = deriveEncoder[AppPublisherExtension]
}


