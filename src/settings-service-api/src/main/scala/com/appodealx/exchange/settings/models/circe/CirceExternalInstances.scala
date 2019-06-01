package com.appodealx.exchange.settings.models.circe

import com.appodealx.exchange.settings.models.buyer.{AppodealResponseStatus, ExternalAgencyCreateRequest, ExternalAgencyCreateResponse, ExternalAgencyUpdateRequest}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}

trait CirceExternalInstances {
  this: CirceBuyerSettingsInstances =>

  implicit val externalAgencyCreateRequestDecoder: Decoder[ExternalAgencyCreateRequest] = deriveDecoder[ExternalAgencyCreateRequest]
  implicit val externalAgencyCreateRequestEncoder: ObjectEncoder[ExternalAgencyCreateRequest] = deriveEncoder[ExternalAgencyCreateRequest]

  implicit val externalAgencyUpdateRequestDecoder: Decoder[ExternalAgencyUpdateRequest] = deriveDecoder[ExternalAgencyUpdateRequest]
  implicit val externalAgencyUpdateRequestEncoder: ObjectEncoder[ExternalAgencyUpdateRequest] = deriveEncoder[ExternalAgencyUpdateRequest]

  implicit val externalAgencyCreateResponseDecoder: Decoder[ExternalAgencyCreateResponse] = deriveDecoder[ExternalAgencyCreateResponse]
  implicit val externalAgencyCreateResponseEncoder: ObjectEncoder[ExternalAgencyCreateResponse] = deriveEncoder[ExternalAgencyCreateResponse]

  implicit val appodealResponseStatusDecoder: Decoder[AppodealResponseStatus] = deriveDecoder[AppodealResponseStatus]
  implicit val appodealResponseStatusEncoder: ObjectEncoder[AppodealResponseStatus] = deriveEncoder[AppodealResponseStatus]
}
