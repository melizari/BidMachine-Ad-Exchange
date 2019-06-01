package com.appodealx.exchange.settings.models.circe

import com.appodealx.exchange.common.models.circe._
import com.appodealx.exchange.settings.models.seller._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait CirceSellerSettingsInstances
  extends CirceModelsInstances
    with CirceMappedToInstances
    with CirceEnumInstances
    with CirceRtbInstances
    with CirceDtoInstances {

  implicit val adSpaceIdDecoder = mappedToDecoder(AdSpaceId.apply)

  implicit val sellerPublisherDecoder = deriveDecoder[Publisher]
  implicit val sellerPublisherEncoder = deriveEncoder[Publisher]

  implicit val appSettingsDecoder = deriveDecoder[AppSettings]
  implicit val appSettingsEncoder = deriveEncoder[AppSettings]

  implicit val sellerAppDecoder = deriveDecoder[App]
  implicit val sellerAppEncoder = deriveEncoder[App]

  implicit val formatDecoder = deriveDecoder[Format]
  implicit val formatEncoder = deriveEncoder[Format]

  implicit val adUnitConfigDecoder = deriveDecoder[AdUnitConfig]
  implicit val adUnitConfigEncoder = deriveEncoder[AdUnitConfig]

  implicit val bannerAdSpaceDecoder = deriveDecoder[BannerAdSpace]
  implicit val bannerAdSpaceEncoder = deriveEncoder[BannerAdSpace]

  implicit val videoAdSpaceDecoder = deriveDecoder[VideoAdSpace]
  implicit val videoAdSpaceEncoder = deriveEncoder[VideoAdSpace]

  implicit val nativeAdSpaceDecoder = deriveDecoder[NativeAdSpace]
  implicit val nativeAdSpaceEncoder = deriveEncoder[NativeAdSpace]
}
