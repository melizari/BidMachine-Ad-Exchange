package services.auction.pb.adapters.rubicon.model

import com.appodealx.exchange.common.models.circe.{CirceEnumInstances, CirceRtbInstances}
import com.appodealx.openrtb.native.LayoutType
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait CirceRubiconInstances extends CirceRtbInstances with CirceEnumInstances{

  implicit val rubiconBidEnc: Encoder[RubiconBid] = deriveEncoder[RubiconBid]
  implicit val rubiconBidDec: Decoder[RubiconBid] = deriveDecoder[RubiconBid]

  implicit val rubiconSitBidEnc: Encoder[RubiconSeatBid] = deriveEncoder[RubiconSeatBid]
  implicit val rubiconSitBidDec: Decoder[RubiconSeatBid] = deriveDecoder[RubiconSeatBid]

  implicit val rubiconSitBidResponseEnc: Encoder[RubiconBidResponse] = deriveEncoder[RubiconBidResponse]
  implicit val rubiconSitBidResponseDec: Decoder[RubiconBidResponse] = deriveDecoder[RubiconBidResponse]

  implicit val rubiconLayoutEnc: Encoder[LayoutType] = deriveEncoder[LayoutType]
  implicit val rubiconLayoutDec: Decoder[LayoutType] = deriveDecoder[LayoutType]

  implicit val rubiconNativeRequestEnc: Encoder[RubiconNativeRequest] = deriveEncoder[RubiconNativeRequest]
  implicit val rubiconNativeRequestDec: Decoder[RubiconNativeRequest] = deriveDecoder[RubiconNativeRequest]
}
