package com.appodealx.exchange.settings

import play.api.libs.functional.syntax._
import com.appodealx.exchange.settings.models.seller.AdSpaceId
import enumeratum.values.{StringEnum, StringEnumEntry}
import play.api.libs.json._
import play.api.mvc.PathBindable
import slick.lifted.MappedTo

import scala.util.{Failure, Success, Try}

package object models {

  object binders {
    def mappedToPathBindable[A <: MappedTo[T], T](b: T => A)(implicit d: PathBindable[T]) = d.transform[A](b, _.value)

    implicit val adSpaceIdPathBindable = mappedToPathBindable(AdSpaceId.apply)
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
