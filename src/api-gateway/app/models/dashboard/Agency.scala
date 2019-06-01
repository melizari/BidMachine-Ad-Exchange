package models.dashboard

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}

case class Agency(agency: Option[String])

object Agency {
  implicit val agencyDecoder: Decoder[Agency] = deriveDecoder[Agency]
  implicit val agencyEncoder: ObjectEncoder[Agency] = deriveEncoder[Agency]
}