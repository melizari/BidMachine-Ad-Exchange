package services.auction.pb.adapters.rubicon.model.extensions

import io.circe.ObjectEncoder
import io.circe.generic.semiauto.deriveEncoder

case class NativeRequestRubiconExtension(privacy: Option[Int] = Some(0))

object NativeRequestRubiconExtension {
  implicit val nativeRequestRubiconExtensionEnc: ObjectEncoder[NativeRequestRubiconExtension] = deriveEncoder[NativeRequestRubiconExtension]
}
