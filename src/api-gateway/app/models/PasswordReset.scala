package models

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}

@ApiModel
case class PasswordReset(password: String,
                         email: String,
                         @ApiModelProperty(name = "expires_at") expiresAt: Long,
                         signature: String)

object PasswordReset {

  implicit val userRegistrationReads: Reads[PasswordReset] = (
    (__ \ "password").read[String] ~
    (__ \ "email").read[String](Reads.email) ~
    (__ \ "expires_at").read[Long] ~
    (__ \ "signature").read[String]
  )(PasswordReset.apply _)

}