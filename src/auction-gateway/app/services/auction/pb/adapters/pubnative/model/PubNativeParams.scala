package services.auction.pb.adapters.pubnative.model

import io.circe.generic.semiauto.deriveDecoder


case class PubNativeParams(`zone_id`: Int, `app_token`: String)

object PubNativeParams {
  implicit val decoder = deriveDecoder[PubNativeParams]
}