package com.appodealx.exchange.druid.transport.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}

case class AgencyDTO(agency: Option[String])

object AgencyDTO {
  implicit val agencyDecoder: Decoder[AgencyDTO] = deriveDecoder[AgencyDTO]
  implicit val agencyEncoder: ObjectEncoder[AgencyDTO] = deriveEncoder[AgencyDTO]
}
