package utils

import com.appodealx.exchange.common.models.{Failure, FailureReason}
import monix.eval.Task

import cats.MonadError
import cats.syntax.flatMap._

object failureutils {

  def liftOption[B](message: String)(o: Option[B]): Task[B] = liftTask(message)(Task.now(o))

  def liftTask[B](message: String)(task: Task[Option[B]]): Task[B] =
    task.flatMap(_.fold(Task.raiseError[B](Failure(FailureReason.InternalFailure, message)))(Task.now))

  def liftToFailureF[F[_], B](message: String)(f: F[Option[B]])(implicit M: MonadError[F, Throwable]): F[B] =
    f.flatMap(_.fold(M.raiseError[B](Failure(FailureReason.InternalFailure, message)))(M.pure))

  def liftOptionToFailureF[F[_], B](message: String)(o: Option[B])(implicit M: MonadError[F, Throwable]): F[B] =
    liftToFailureF(message)(M.pure(o))

}
