package silhouette

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class PersistentUser(id: Option[Long],
                          email: String,
                          role: UserRole,
                          name: Option[String],
                          company: Option[String]) extends Identity

object PersistentUser {

  implicit val userReads = (
    (__ \ "id").readNullable[Long] ~
      (__ \ "email").read[String](Reads.email) ~
      (__ \ "role").read[UserRole] ~
      (__ \ "name").readNullable[String] ~
      (__ \ "company").readNullable[String]
    ) (PersistentUser.apply _)

  implicit val userWrites = Json.writes[PersistentUser]

}