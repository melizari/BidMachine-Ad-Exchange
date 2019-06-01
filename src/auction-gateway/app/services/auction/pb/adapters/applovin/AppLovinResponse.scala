package services.auction.pb.adapters.applovin

import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Decoder, ObjectEncoder}


case class AppLovinResponse(`zone_id`: String, `ad_token`: String)

object AppLovinResponse {
  implicit val appLovinResponseDecoder: Decoder[AppLovinResponse] =
    Decoder.forProduct2("zone_id", "adtoken")(AppLovinResponse.apply)

  implicit val appLovinResponseEncoder: ObjectEncoder[AppLovinResponse] = deriveEncoder[AppLovinResponse]

}
