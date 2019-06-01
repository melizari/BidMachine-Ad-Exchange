package controllers.adtracker

import com.appodealx.exchange.common.models.analytics.TrackingEventType.{ErrorMraidEvent, ErrorNastEvent, ErrorVastEvent}
import monix.execution.Scheduler
import play.api.Logger
import play.api.mvc._
import services.AdErrorTrackerService


class AdErrorTrackerController(adErrorTrackerService: AdErrorTrackerService,
                               controllerComponents: ControllerComponents)(implicit ec: Scheduler)
  extends AbstractController(controllerComponents) {

  def vastErrorTyped(adType: String) = Action { implicit request =>
    adErrorTrackerService.trackError(ErrorVastEvent)
    Logger.debug("VAST error tracked by type " + adType)
    Ok
  }

  def mraidErrorTyped(adType: String) = Action { implicit request =>
    adErrorTrackerService.trackError(ErrorMraidEvent)
    Logger.debug("MRAID error tracked by type " + adType)
    Ok
  }

  def nastErrorTyped(adType: String) = Action { implicit request =>
    adErrorTrackerService.trackError(ErrorNastEvent)
    Logger.debug("NAST error tracked by type " + adType)
    Ok
  }

}