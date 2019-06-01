package models

import play.api.libs.json.Json

case class BasicAuthPasswordUpdate(current: Option[String], next: String)

object BasicAuthPasswordUpdate {

  implicit val passwordUpdateFormat = Json.format[BasicAuthPasswordUpdate]

}