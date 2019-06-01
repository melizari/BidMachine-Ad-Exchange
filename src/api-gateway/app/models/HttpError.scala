package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class HttpError(code: Int, message: String, exception: Option[Exception] = None) extends RuntimeException(message, exception.orNull)

object HttpError {

  implicit val errorWrites = (
    (__ \ "code").write[Int] ~
    (__ \ "message").write[String] ~
    (__ \ "exception").writeNullable[String].contramap[Option[Exception]](_.map(_.getClass.getSimpleName))
  )(unlift(HttpError.unapply))

}
