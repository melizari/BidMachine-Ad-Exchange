package com.appodealx.exchange.druid.models.scruid.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


case class CountryResponse(country: Option[String], count: Int)

object CountryResponse {
  implicit val countryResponseEncoder = deriveEncoder[CountryResponse]
  implicit val countryResponseDecoder = deriveDecoder[CountryResponse]
}