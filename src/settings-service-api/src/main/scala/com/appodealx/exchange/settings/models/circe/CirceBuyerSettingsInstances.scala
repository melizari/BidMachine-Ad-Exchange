package com.appodealx.exchange.settings.models.circe

import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.settings.models.buyer._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait CirceBuyerSettingsInstances
  extends CirceModelsInstances
    with CirceExternalInstances {

  implicit val bannerAdProfileDecoder = deriveDecoder[BannerAdProfile]
  implicit val bannerAdProfileEncoder = deriveEncoder[BannerAdProfile]

  implicit val videoAdProfileDecoder = deriveDecoder[VideoAdProfile]
  implicit val videoAdProfileEncoder = deriveEncoder[VideoAdProfile]

  implicit val nativeAdProfileDecoder = deriveDecoder[NativeAdProfile]
  implicit val nativeAdProfileEncoder = deriveEncoder[NativeAdProfile]

  implicit val bannerAdProfileWithBidderDecoder = deriveDecoder[BannerAdProfileWithBidder]
  implicit val bannerAdProfileWithBidderEncoder = deriveEncoder[BannerAdProfileWithBidder]

  implicit val videoAdProfileWithBidderDecoder = deriveDecoder[VideoAdProfileWithBidder]
  implicit val videoAdProfileWithBidderEncoder = deriveEncoder[VideoAdProfileWithBidder]

  implicit val nativeAdProfileWithBidderDecoder = deriveDecoder[NativeAdProfileWithBidder]
  implicit val nativeAdProfileWithBidderEncoder = deriveEncoder[NativeAdProfileWithBidder]
}
