package controllers.adtracker

import play.api.Logger
import play.api.mvc._
import services.AdTrackerService
import com.appodealx.exchange.common.models.analytics.TrackingEventType._
import monix.execution.Scheduler


class AdTrackerController(adTrackerService: AdTrackerService,
                          controllerComponents: ControllerComponents)
                         (implicit val scheduler: Scheduler)
  extends AbstractController(controllerComponents) {

  /**
    * Impression tracking endpoint
    */
  def impressionTyped(adType: String) = Action.async { implicit request =>
    Logger.debug("Impression tracked by type " + adType)
    adTrackerService.track(ImpressionEvent).map(_ => Ok)
  }

  /**
    * Clicks tracking endpoint
    */
  def clickTyped(adType: String) = Action.async { implicit request =>
    Logger.debug("Click tracked by type " + adType)
    adTrackerService.track(ClickEvent).map(_ => Ok)
  }

  /**
    * Finish tracking endpoint
    */
  def finishTyped(adType: String) = Action.async { implicit request =>
    Logger.debug("Finish tracked by type " + adType)
    adTrackerService.track(FinishEvent).map(_ => Ok)
  }

  /**
    * Fill tracking endpoint
    */
  def fillTyped(adType: String) = Action.async { implicit request =>
    Logger.debug("Fill tracked by type " + adType)
    adTrackerService.track(FillEvent).map(_ => Ok)
  }

  /**
    * Custom pixel tracking endpoint
    */
  def customEventTyped(adType: String) = Action.async { implicit request =>
    Logger.debug("Custom pixel tracked by type " + adType)
    adTrackerService.track(CustomEvent).map(_ => Ok)
  }
}
