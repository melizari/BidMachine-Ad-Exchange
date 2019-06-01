package models.mandrill

import io.circe.generic.semiauto.deriveEncoder

case class Message(html: Option[String] = None,
                   text: Option[String] = None,
                   subject: Option[String],
                   from_email: String,
                   from_name: String,
                   to: List[To],
                   merge_vars: List[MergeVar])

object Message {
  implicit val encoder = deriveEncoder[Message]
}