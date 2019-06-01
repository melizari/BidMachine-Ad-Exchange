package models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}

case class LoginPass(login: String, password: String)

object LoginPass {
  implicit val agencyDecoder: Decoder[LoginPass] = deriveDecoder[LoginPass]
  implicit val agencyEncoder: ObjectEncoder[LoginPass] = deriveEncoder[LoginPass]
}