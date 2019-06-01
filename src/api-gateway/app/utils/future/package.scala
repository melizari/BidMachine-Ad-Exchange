package utils

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

package object future {

  implicit class FutureMapComplete[T](future: Future[T]) {
    def mapComplete[U](f: Try[T] => U)(implicit ec: ExecutionContext): Future[U] = {
      val promise = Promise[U]()
      future onComplete (f andThen promise.success)
      promise.future
    }
  }
}
