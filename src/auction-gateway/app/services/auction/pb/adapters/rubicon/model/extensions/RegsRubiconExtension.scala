package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.ObjectEncoder
import io.circe.generic.semiauto.deriveEncoder

case class RegsRubiconExtension(s22580: Option[Int],
                                gdpr: Option[Int])

object RegsRubiconExtension {
  implicit val regsRubiconExtensionEnc: ObjectEncoder[RegsRubiconExtension] = deriveEncoder[RegsRubiconExtension]
}
