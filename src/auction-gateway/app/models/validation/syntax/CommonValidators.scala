package models.validation.syntax
import models.validation.{ValidationError, Validator}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

import scala.util.Try

trait CommonValidators {

  import cats.syntax.validated._

  def failure(message: String) = ValidationError(Nil, message)

  def beSameStringIgnoringCase(expected: String): Validator[String, String] = actual => {
    beEqualTo(expected.toLowerCase).validate(actual.toLowerCase)
  }

  val beNonEmptyString: Validator[String, String] = s =>
    if (s.isEmpty) {
      failure("non empty string expected").invalidNel
    } else {
      s.validNel
    }

  def beEqualTo[A](expected: A): Validator[A, A] = actual => {
    if(actual == expected) {
      actual.validNel
    } else {
      failure("must be equal to " + expected.toString).invalidNel
    }
  }

  def filter[A](f: A => Boolean, message: String): Validator[A, A] = a => {
    if(f(a)) {
      a.validNel
    } else {
      failure(message).invalidNel
    }
  }

  def beEqualToOneOf[A](a:A, as: A*): Validator[A, A] = value => {
    if (value == a || as.contains(value)) {
      a.validNel
    } else {
      failure(s"must be equal to one of ${as.mkString(",")}").invalidNel
    }
  }


  def beParsedAsLong: Validator[String, Long] = s => {
    val stringAsLong = Try(s.toLong).toOption

    if (stringAsLong.isDefined) {
      stringAsLong.get.validNel
    } else {
      failure("must be a numeric value").invalidNel
    }
  }

  def bePositiveInt: Validator[Int, Int] = i => {
    if (i >= 0) {
      i.validNel
    } else {
      failure("must be positive").invalidNel
    }
  }

  def bePositiveDouble: Validator[Double, Double] = i => {
    if (i >= 0.0) {
      i.validNel
    } else {
      failure("must be positive").invalidNel
    }
  }

  def beOptionOfAny[A <: GeneratedMessage with Message[A] : GeneratedMessageCompanion]: Validator[Option[com.google.protobuf.any.Any], _] = a => {
    if (a.isEmpty) {
      failure("must be provided").invalidNel
    } else if (!a.exists(_.is[A])) {
      failure("wrong type of Any instance").invalidNel
    } else {
      a.validNel
    }
  }

  def beSeqOfAny[A <: GeneratedMessage with Message[A] : GeneratedMessageCompanion]: Validator[Seq[com.google.protobuf.any.Any], _] = a => {
    if (a.isEmpty) {
      failure("must be provided").invalidNel
    } else if (!a.exists(_.is[A])) {
      failure("wrong type of Any instance").invalidNel
    } else {
      a.validNel
    }
  }

  def invalid[A](path: String, message: String): Validator[A, A] = _ => { ValidationError(List(path), message).invalidNel }

  def valid[A](a: A): Validator[A, A] = Validator.const(a)
}
