import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.mohiva.play.silhouette.impl.exceptions._
import models.HttpError
import play.api.http.HttpErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Results}
import play.api.{Logger, http}

import scala.concurrent.Future

class ErrorHandler extends HttpErrorHandler with Results {
  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(Status(statusCode)(Json.obj("message" -> message)))
  }

  override def onServerError(request: RequestHeader, exception: Throwable) = {
    Logger.error(exception.getMessage, exception)

    val httpError: HttpError = exception match {
      case _: IdentityNotFoundException =>
        HttpError(code = http.Status.BAD_REQUEST, message = "User not found")

      case _: InvalidPasswordException =>
        HttpError(code = http.Status.BAD_REQUEST, message = "Incorrect password")

      case e: HttpError => e

      case e: TransportException => HttpError(e.errorCode.http, message = e.exceptionMessage.detail)

      case e: Exception =>
        HttpError(code = http.Status.INTERNAL_SERVER_ERROR,
          message = e.getLocalizedMessage,
          exception = Some(e))
    }

    Future.successful(Status(httpError.code)(Json.toJson(httpError)))
  }
}
