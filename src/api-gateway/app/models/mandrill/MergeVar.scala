package models.mandrill

import io.circe.generic.semiauto.deriveEncoder

case class MergeVar(rcpt: String, vars: List[Var])

object MergeVar {
  implicit val encoder = deriveEncoder[MergeVar]
}
