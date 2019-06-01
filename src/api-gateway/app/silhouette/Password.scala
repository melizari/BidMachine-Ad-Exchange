package silhouette

import play.api.libs.json.Json

case class Password(current: String, next: String)

object Password {

  implicit val passwordUpdateFormat = Json.format[Password]

}