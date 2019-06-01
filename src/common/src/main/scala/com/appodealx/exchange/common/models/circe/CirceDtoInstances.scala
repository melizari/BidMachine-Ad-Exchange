package com.appodealx.exchange.common.models.circe

import com.appodealx.exchange.common.models.dto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait CirceDtoInstances { self: CirceRtbInstances =>

  implicit val dtoBannerDecoder = deriveDecoder[dto.Banner]
  implicit val dtoBannerEncoder = deriveEncoder[dto.Banner]

  implicit val dtoVideoDecoder = deriveDecoder[dto.Video]
  implicit val dtoVideoEncoder = deriveEncoder[dto.Video]

  implicit val dtoNativeDecoder = deriveDecoder[dto.Native]
  implicit val dtoNativeEncoder = deriveEncoder[dto.Native]

}
