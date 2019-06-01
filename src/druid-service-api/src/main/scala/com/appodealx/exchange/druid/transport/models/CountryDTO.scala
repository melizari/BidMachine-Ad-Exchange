package com.appodealx.exchange.druid.transport.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}

case class CountryDTO(code: String, title: String)

object CountryDTO {
  implicit val countryResultDecoder: Decoder[CountryDTO] = deriveDecoder[CountryDTO]
  implicit val countryResultEncoder: ObjectEncoder[CountryDTO] = deriveEncoder[CountryDTO]
}
