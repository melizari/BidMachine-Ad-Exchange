import com.appodealx.exchange.common.models.Failure
import com.appodealx.exchange.common.models.FailureReason.{RequestDecodingFailure, RequestMissingParametersFailure, RequestValidatingFailure}
import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future


class CustomErrorHandler
  extends HttpErrorHandler {

  private val logger = Logger(getClass)

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(Status(statusCode).withHeaders("ad-exchange-error-message" -> message))
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    logger.error(exception.getMessage, exception)

    val message = exception.getMessage.replaceAll("\r", "").replaceAll("\n", "")

    val result = {
      val defaultHeader = "ad-exchange-error-message" -> message

      exception match {
        case f @ Failure(RequestDecodingFailure, _) => BadRequest.withHeaders(defaultHeader, "ad-exchange-error-reason" -> f.reason.entryName)
        case f @ Failure(RequestValidatingFailure, _) => BadRequest.withHeaders(defaultHeader, "ad-exchange-error-reason" -> f.reason.entryName)
        case f @ Failure(RequestMissingParametersFailure, _) => BadRequest.withHeaders(defaultHeader, "ad-exchange-error-reason" -> f.reason.entryName)
        case f: Failure => NoContent.withHeaders(defaultHeader, "ad-exchange-error-reason" -> f.reason.entryName)
        case _ => NoContent.withHeaders(defaultHeader)
      }
    }
    Future.successful(result)
  }
}