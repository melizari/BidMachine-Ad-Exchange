
import enumeratum.values.{StringEnum, StringEnumEntry}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.PathBindable

import scala.util.{Failure, Success, Try}


package object silhouette {

  object binders {

    implicit def pathBinderResourceType(implicit d: PathBindable[String]) = new PathBindable[ResourceType] {

      override def bind(key: String, value: String): Either[String, ResourceType] = {

        ResourceType.values.find(_.value == value) match {
          case Some(x) => Right(x)
          case None => Left("Error! No such resource type")
        }
      }

      override def unbind(key: String, value: ResourceType): String = value.value
    }
  }

  implicit class RichReads[A](reads: Reads[A]) {
    def resultMap[B](f: A => JsResult[B]): Reads[B] = Reads(reads.reads(_).flatMap(f))
  }

  implicit class RichTry[A](t: Try[A]) {
    def toJsonResult = t match {
      case Success(result) => JsSuccess(result)
      case Failure(e) => JsError(e.getLocalizedMessage)
    }
  }

  implicit def stringEnumWrites[A <: StringEnumEntry](implicit enum: StringEnum[A]): Writes[A] = {
    Writes.StringWrites.contramap(_.value)
  }

  implicit def stringEnumReads[A <: StringEnumEntry](implicit enum: StringEnum[A]): Reads[A] = {
    Reads.StringReads.resultMap { value =>
      Try(enum.withValue(value)).toJsonResult
    }
  }

}
