package controllers.adtracker

import java.util.UUID

import com.appodealx.exchange.common.models.{CallbackParams, Failure, FailureReason}
import com.appodealx.exchange.common.models.analytics.{TrackingEventType, TrackingExtendedEventType}
import io.bidmachine.protobuf.EventTypeExtended
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader, Result}
import services.{AdErrorTrackerService, AdTrackerService}

import cats.syntax.either._
import monix.execution.Scheduler
import play.api.Logger

import scala.concurrent.Future
import scala.util.Try


class EventTrackerController(controllerComponents: ControllerComponents,
                             adTrackerService: AdTrackerService,
                             adErrorTrackerService: AdErrorTrackerService)
                            (implicit val scheduler: Scheduler)
  extends AbstractController(controllerComponents) {

  private val logger = Logger(getClass)

  // All tracking and errors must be here
  def event = Action.async { implicit request =>

    val eventTypeMayBe = request
      .getQueryString(CallbackParams.EventCodeParam)
      .fold(Failure(FailureReason.RequestMissingParametersFailure, "no event type provided.").asLeft[EventTypeExtended])(eventTypeExtFromString)

    eventTypeMayBe.fold(internalServerError, result)
  }

  private def internalServerError(failure: Failure) = {
    val errorId = UUID.randomUUID()
    logger.error(s"$errorId : " + failure.getMessage, failure)
    Future.successful(InternalServerError(s"Something went wrong: $errorId"))
  }

  private def eventTypeExtFromString(string: String) =
    Try(EventTypeExtended.fromValue(string.toInt))
      .fold(e => Failure(FailureReason.RequestDecodingFailure, e.getMessage).asLeft, str => str.asRight)

  private def result(eventTypeExt: EventTypeExtended)(implicit requestHeader: RequestHeader): Future[Result] = eventTypeExt match {
    case EventTypeExtended.EVENT_TYPE_EXTENDED_IMPRESSION =>
      adTrackerService.track(TrackingEventType.ImpressionEvent).map(_ => Ok)
    case EventTypeExtended.EVENT_TYPE_EXTENDED_CLICK =>
      adTrackerService.track(TrackingEventType.ClickEvent).map(_ => Ok)
    case EventTypeExtended.EVENT_TYPE_EXTENDED_LOADED =>
      adTrackerService.track(TrackingEventType.FillEvent).map(_ => Ok)
    case EventTypeExtended.EVENT_TYPE_EXTENDED_CLOSED =>
      adTrackerService.track(TrackingEventType.FinishEvent).map(_ => Ok)
    case EventTypeExtended.EVENT_TYPE_EXTENDED_DESTROYED =>
      logger.warn("\n##############################################\n### Destroyed tracker not implemented yet! ###\n##############################################")
      Future.successful(Ok)
    case EventTypeExtended.EVENT_TYPE_EXTENDED_VIEWABLE=>
      logger.warn("\n###########################################\n## Viewable tracker not implemented yet! ##\n###########################################")
      Future.successful(Ok)
    case EventTypeExtended.EVENT_TYPE_EXTENDED_ERROR | EventTypeExtended.EVENT_TYPE_EXTENDED_TRACKING_ERROR =>
      adErrorTrackerService.trackEvent.map(_ => Ok)
    case EventTypeExtended.Unrecognized(TrackingExtendedEventType.`CUSTOM_LOADED_EVENT`) =>
      adTrackerService.track(TrackingEventType.CustomEvent).map(_ => Ok)
    case eventType => Future.successful(BadRequest(s"Not supported action with code ${eventType.value}"))
  }
}
