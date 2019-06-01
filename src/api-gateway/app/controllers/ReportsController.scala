package controllers

import java.io.ByteArrayInputStream
import java.util.UUID

import akka.stream.scaladsl.StreamConverters
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.druid.DruidService
import com.digitaltangible.playguard.RateLimiter
import com.lightbend.lagom.scaladsl.api.transport.TransportException
import com.mohiva.play.silhouette.api.Silhouette
import io.circe.Printer
import io.swagger.annotations.Api
import monix.execution.Scheduler
import org.joda.time.{DateTime, Days}
import play.api.libs.circe.Circe
import play.api.mvc._
import play.api.{Configuration, Logger}
import silhouette.{IdentityLimiter, ResourceAuthorizations, SellerEnvBearer, UserRole}

import scala.concurrent.Future
import scala.util.Try

@Api(value = "Reports",
  produces = "application/json",
  consumes = "application/json")
class ReportsController(druid: DruidService,
                        configuration: Configuration,
                        silhouette: Silhouette[SellerEnvBearer],
                        cc: ControllerComponents)(implicit scheduler: Scheduler)

  extends AbstractController(cc)
    with ResourceAuthorizations[SellerEnvBearer]
    with Circe
    with CirceModelsInstances {

  private val logger = Logger(getClass)

  private val AD_EXCHANGE_ERROR_MESSAGE_HEADER = "ad-exchange-error-message"

  implicit val customPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  private val config = configuration.get[Configuration]("reporting.ssp")

  private val size = config.get[Int]("rate-limit.size")
  private val rate = config.get[Double]("rate-limit.rate").toFloat

  private val limiter = new RateLimiter(size, rate, "SSP Report request limiter")
  private val rejectResult = Results.TooManyRequests.withHeaders(AD_EXCHANGE_ERROR_MESSAGE_HEADER -> "You've been requesting too much. Please try again in 5 seconds.")

  val SellerAction = silhouette.SecuredAction(WithAccountRole(UserRole.Seller))
  val ReportAction = SellerAction andThen IdentityLimiter(limiter)(_ => rejectResult)

  // Endpoint for ssp reporting
  def reportSSP(start: String, end: String) = ReportAction.async { implicit request =>

    val st = System.currentTimeMillis()

    val sellerId = request.identity.resource.id

    val format = request.getQueryString("format")
    val csvWithHeader = request.getQueryString("csv_header").flatMap(string => Try(string.toInt > 0).toOption)

    def isOneDayInterval(s: String, e: String) = {
      val startDate = DateTime.parse(s)
      val endDate = DateTime.parse(e)
      Days.daysBetween(startDate, endDate).getDays <= 1
    }

    sellerId match {
      case Some(id) if isOneDayInterval(start, end) =>
        val result = druid
          .sspReport(id, start, end, format, csvWithHeader)
          .invoke()
          .map { string =>
            val inputStream = new ByteArrayInputStream(string.getBytes("UTF-8"))
            val stream = StreamConverters.fromInputStream(() => inputStream)
            Ok.chunked(stream)
          }
          .recover {
            case t: TransportException =>
              logger.error("TransportException from drud service: " + t.exceptionMessage)
              new Status(t.errorCode.http)
            case e: Exception =>
              val errorId = UUID.randomUUID()
              logger.error(s"$errorId : ${e.getMessage}", e)
              InternalServerError.withHeaders(AD_EXCHANGE_ERROR_MESSAGE_HEADER -> s"Something went wrong: $errorId")
          }
        result.onComplete(_ => logger.debug(s"reportSSP: elapsed time ${System.currentTimeMillis() - st} ms"))
        result
      case Some(_) => Future.successful(BadRequest.withHeaders(AD_EXCHANGE_ERROR_MESSAGE_HEADER -> "Requested interval more then one day"))
      case None    => Future.successful(Forbidden.withHeaders(AD_EXCHANGE_ERROR_MESSAGE_HEADER -> "No SSP associated for this account."))
    }
  }
}