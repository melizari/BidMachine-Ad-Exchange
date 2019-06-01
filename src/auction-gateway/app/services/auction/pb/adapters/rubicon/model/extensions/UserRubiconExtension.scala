package services.auction.pb.adapters.rubicon.model.extensions

import com.appodealx.openrtb.Json
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class UserRubiconExtension(rp: Option[Json] = None,
                                dt: Option[Json] = None,
                                consent: Option[String] = None)

object UserRubiconExtension {
  implicit val userRubiconExtensionEnc: Encoder[UserRubiconExtension] = deriveEncoder[UserRubiconExtension]
}

