package controllers.auction

import controllers.helpers.{CustomBaseController, CustomControllerComponents}
import io.bidmachine.protobuf.{InitRequest, InitResponse}
import models.DefaultWriteables
import monix.eval.Task
import monix.execution.Scheduler
import services.geo.GeoUtils
import services.init.InitService

import scala.language.postfixOps

class InitController(initService: InitService[Task], cc: CustomControllerComponents)(implicit val scheduler: Scheduler)
    extends CustomBaseController(cc)
    with DefaultWriteables {

  private val noEndpointProviderHeader = "ad-exchange-error-reason" -> "None endpoints was configured"

  def init = Action.async(parse.protobuf[InitRequest]) { implicit request =>
    def ok(r: InitResponse) = Task pure Ok(r)
    def notFound            = Task pure NotFound.withHeaders(noEndpointProviderHeader)

    val ip = GeoUtils.ipFromHeaders(request.headers)

    {
      for {
        response <- initService.init(ip)
        result   <- response.fold(notFound)(ok)
      } yield result
    } runToFuture
  }
}
