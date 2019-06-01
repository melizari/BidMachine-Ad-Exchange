package models.circe

import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import com.appodealx.exchange.settings.models.circe.CirceSellerSettingsInstances
import io.circe.Decoder
import io.circe.generic.semiauto._
import models._


trait CirceAuctionSettingsInstances extends CirceSellerSettingsInstances with CirceEnumInstances {

  implicit val appodealExtensionDecoder: Decoder[BidRequestExtension] = deriveDecoder[BidRequestExtension]
  implicit val sdkAppExtensionDecoder: Decoder[SessionExtension] = deriveDecoder[SessionExtension]
  implicit val sessionImpressionDecoder: Decoder[SessionMetrics] = deriveDecoder[SessionMetrics]

}
