package silhouette

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(id: Option[Long],
                email: String,
                role: UserRole,
                name: Option[String],
                company: Option[String],
                permissions: Vector[Permission]) extends Identity

object User {

  implicit val userReads = (
    (__ \ "id").readNullable[Long] ~
      (__ \ "email").read[String](Reads.email) ~
      (__ \ "role").read[UserRole] ~
      (__ \ "name").readNullable[String] ~
      (__ \ "company").readNullable[String] ~
      Reads.pure(Vector.empty)
    ) (User.apply _)

  implicit val userWrites = Json.writes[User]

}