package models.mandrill

import io.circe.generic.semiauto.deriveEncoder

case class To(email: String, Name: String)

object To {
  implicit val encoder = deriveEncoder[To]
}