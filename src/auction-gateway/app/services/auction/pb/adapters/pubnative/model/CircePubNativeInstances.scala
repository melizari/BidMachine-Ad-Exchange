package services.auction.pb.adapters.pubnative.model

import com.appodealx.exchange.common.models.circe.CirceEnumInstances
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


trait CircePubNativeInstances extends CirceEnumInstances {

  implicit val pubNativeResponseDecoder: Decoder[PubNativeResponse] = deriveDecoder[PubNativeResponse]
  implicit val pubNativeResponseEncoder: Encoder[PubNativeResponse] = deriveEncoder[PubNativeResponse]

  implicit val pubNativeAdDecoder: Decoder[PubNativeAd] = deriveDecoder[PubNativeAd]
  implicit val pubNativeAdEncoder: Encoder[PubNativeAd] = deriveEncoder[PubNativeAd]

  implicit val pNAssetDecoder: Decoder[PubNativeAsset] = deriveDecoder[PubNativeAsset]
  implicit val pNAssetEncoder: Encoder[PubNativeAsset] = deriveEncoder[PubNativeAsset]

  implicit val metaDataDecoder: Decoder[MetaData] = deriveDecoder[MetaData]
  implicit val metaDataEncoder: Encoder[MetaData] = deriveEncoder[MetaData]

  implicit val metaDecoder: Decoder[Meta] = deriveDecoder[Meta]
  implicit val metaEncoder: Encoder[Meta] = deriveEncoder[Meta]

  implicit val assetDataDecoder: Decoder[AssetData] = deriveDecoder[AssetData]
  implicit val assetDataEncoder: Encoder[AssetData] = deriveEncoder[AssetData]

  implicit val beaconsDataDecoder: Decoder[BeaconData] = deriveDecoder[BeaconData]
  implicit val beaconsDataEncoder: Encoder[BeaconData] = deriveEncoder[BeaconData]

  implicit val beaconDecoder: Decoder[Beacon] = deriveDecoder[Beacon]
  implicit val beaconEncoder: Encoder[Beacon] = deriveEncoder[Beacon]

}
