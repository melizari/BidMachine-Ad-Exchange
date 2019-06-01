package models.mandrill

import io.circe.generic.semiauto.deriveEncoder

case class TemplateContent(name: String, content: String)

object TemplateContent {
  implicit val encoder = deriveEncoder[TemplateContent]
}