package services

import com.appodealx.exchange.common.models.Failure
import com.appodealx.exchange.common.models.FailureReason.RequestDecodingFailure
import models.validation.Validator.Result
import models.validation.{ValidationError, Validator}

import cats.MonadError
import cats.data.Validated
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.validated._

import scala.language.higherKinds

trait ValidationService[F[_]] {

  implicit def M: MonadError[F, Throwable]

  def validate[A](a: A)(implicit V: Validator[A, A]): F[A]

  protected def unpack[A](a: => A): F[A]

  def unpackAndValidate[A](a: => A)(implicit V: Validator[A, A]): F[A] =
    for {
      ua <- unpack(a)
      va <- validate(ua)
    } yield va
}

final class ValidationServiceImpl[F[_]](
  implicit val M: MonadError[F, Throwable]
) extends ValidationService[F] {

  def validate[A](a: A)(implicit V: Validator[A, A]): F[A] = {

    def errorMessage(e: ValidationError) = e.pathComponents.mkString(".") + " " + e.message

    val result: Result[A] = V.validate(a)

    val resultWithCombinedErrors: Validated[Throwable, A] = result leftMap { nel =>
      ValidationError(Nil, nel.map(errorMessage).reduce[String](_ + ";" + _))
    }

    resultWithCombinedErrors.liftTo[F]
  }

  protected def unpack[A](a: => A): F[A] =
    M.catchNonFatal(a).adaptError { case e => Failure(RequestDecodingFailure, e.getMessage) }
}
