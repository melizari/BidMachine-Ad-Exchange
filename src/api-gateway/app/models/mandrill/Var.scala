package models.mandrill

import io.circe.generic.semiauto.deriveEncoder

case class Var(name: String, content: String)

object Var {
  implicit val encoder = deriveEncoder[Var]
}