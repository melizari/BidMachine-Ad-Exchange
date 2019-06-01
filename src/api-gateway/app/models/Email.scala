package models

import play.api.libs.json.Json

case class Email(email: String)

object Email {
  implicit val format = Json.format[Email]
}