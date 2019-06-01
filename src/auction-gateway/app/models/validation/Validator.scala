package models.validation

import cats.Applicative
import cats.data.ValidatedNel
import models.validation.Validator._
import cats.syntax.validated._
import cats.syntax.apply._

trait Validator[In, A] { self =>

  def validate(a: In): Result[A]

  def leftMap(f: Error => Error): Validator[In, A] = in => {
    self.validate(in).leftMap(_.map(f))
  }

  def andThen[B](another: Validator[In, _]): Validator[In, _] = in => {
    self.validate(in).andThen(_ => another.validate(in))
  }
}

object Validator {

  type Error = ValidationError
  type Result[A] = ValidatedNel[Error, A]

  def apply[A](implicit v: Validator[A, A]): Validator[A, A] = v

  def instance[In, Out](f: In => Result[Out]): Validator[In, Out] = (a: In) => f(a)

  def of[A](f: A => Validator[A, _]): Validator[A, A] = a => {
    (f(a) *> Validator.const(a)).validate(a)
  }

  def all[A](f: A => List[Validator[A, _]]): Validator[A, A] = a => {
    val allValidationApplied = f(a).reduce((v1, v2) => v1 *> v2)

    (allValidationApplied *> Validator.const(a)).validate(a)
  }

  implicit def validatorApplicative[In]: Applicative[Validator[In, ?]] = new Applicative[Validator[In, ?]] {

    override def pure[A](x: A): Validator[In, A] = _ => x.validNel[Error]

    override def ap[A, B](ff: Validator[In, A => B])(fa: Validator[In, A]): Validator[In, B] = in => {
      ff.validate(in) <*> fa.validate(in)
    }
  }

  def const[A](a: A): Validator[A, A] = _ => a.validNel

  def unit[A]: Validator[A, A] = _.validNel

}


