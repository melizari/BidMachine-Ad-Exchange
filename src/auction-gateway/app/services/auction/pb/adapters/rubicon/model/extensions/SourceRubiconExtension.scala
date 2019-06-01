package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.ObjectEncoder
import io.circe.generic.semiauto.deriveEncoder

case class SourceRubiconExtension(ssreq: Option[Int])

object SourceRubiconExtension {
  implicit val sourceRubiconExtensionEnc: ObjectEncoder[SourceRubiconExtension] = deriveEncoder[SourceRubiconExtension]
}
