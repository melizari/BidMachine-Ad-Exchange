package controllers.auction

import com.appodealx.exchange.common.models.Failure
import com.appodealx.exchange.common.models.auction.{Adm, Plc}
import com.appodealx.exchange.common.models.dto.{Banner, Native, Video}
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.exchange.common.services.GlobalConfigService
import com.appodealx.openrtb.native.response
import controllers.actions.Actions
import controllers.auction.renderers.PbAdMarkupRendering
import models.auction.AdUnit
import models.{SdkRequest, _}
import monix.eval.Task
import monix.execution.Scheduler
import play.api.Logger
import play.api.http.HeaderNames
import play.api.libs.circe.Circe
import play.api.mvc._
import play.twirl.api.Html
import services.auction.pb.PbAuctionService

import scala.util.Try

class PbAuctionController(auctionService: PbAuctionService[Task], cc: ControllerComponents)(
  implicit val scheduler: Scheduler,
  val globalConfig: GlobalConfigService[Task]
) extends AbstractController(cc)
    with PbAdMarkupRendering
    with Circe
    with Actions {

  private val logger = Logger(this.getClass)

  def banner = auction[Html, Banner]
  def video  = auction[VAST, Video]
  def native = auction[response.Native, Native]

  private def auction[A: Adm, P: Plc] = NoFillAction.async(circe.json[List[AdUnit]]) { implicit request =>
    val host = RequestHost(request.host.split(":", 2)(0))

    val result = for {
      sdkRq <- sdkRequestFrom(request)
      gs    <- globalConfig.read
      ad    <- auctionService.perform(sdkRq, request.body, host, gs.tMax)
      r     = ad.fold(NoContent)(renderAd[P])
    } yield r

    result
      .onErrorRecover(addErrorHeaders)
      .runToFuture
  }

  private def sdkRequestFrom(rq: Request[List[AdUnit]]) = {
    val params = rq.queryString.collect {
      case (k, v) if v.nonEmpty && v.head.trim.nonEmpty => (k, v.head)
    }
    val ua = rq.headers.get(HeaderNames.USER_AGENT).map("ua" -> _)
    Task.fromTry(Try(SdkRequest.fromMap(params ++ ua)))
  }

  private def addErrorHeaders: PartialFunction[Throwable, Result] = {
    case f: Failure =>
      NoContent.withHeaders(
        "ad-exchange-error-message" -> f.message,
        "ad-exchange-error-reason"  -> f.reason.entryName
      )
    case e: Exception =>
      logger.error(e.getMessage, e)
      NoContent.withHeaders("ad-exchange-error-message" -> e.getMessage)
  }
}
