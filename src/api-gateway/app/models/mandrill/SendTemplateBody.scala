package models.mandrill

import io.circe.generic.semiauto.deriveEncoder

case class SendTemplateBody(key: String,
                            template_name: String,
                            template_content: List[TemplateContent],
                            message: Message)

object SendTemplateBody {
  implicit val encoder = deriveEncoder[SendTemplateBody]
}

