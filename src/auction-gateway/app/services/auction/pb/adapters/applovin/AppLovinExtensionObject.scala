package services.auction.pb.adapters.applovin

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}


case class AppLovinExtensionObject(`zone_id`: String, `sdk_key`: String)

object AppLovinExtensionObject {
  implicit val appLovinExtObjectDecoder: Decoder[AppLovinExtensionObject] = deriveDecoder[AppLovinExtensionObject]
  implicit val appLovinExtObjectEncoder: ObjectEncoder[AppLovinExtensionObject] = deriveEncoder[AppLovinExtensionObject]

}