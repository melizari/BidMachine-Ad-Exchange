package services

import akka.http.scaladsl.model.Uri
import com.appodealx.exchange.common.models.analytics.{CallbackContext, TrackingEventType, UrlStateDecoder}
import com.appodealx.exchange.common.models.circe.CirceAnalyticsInstances
import com.appodealx.exchange.common.models.{CallbackParams, CallbackTrackingMethod}
import com.appodealx.exchange.common.services.ParamsSigner
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import io.circe.Printer
import io.circe.syntax._
import monix.eval.Task
import monix.execution.Scheduler
import org.joda.time._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.{Configuration, Logger}

import cats.data.OptionT
import cats.instances.future._
import cats.syntax.option._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class AdTrackerService(
  ws: WSClient,
  configuration: Configuration,
  emitterService: EmitterService,
  signService: ParamsSigner,
  deDuplicatingChecker: DeDuplicatingChecker,
  customFillTime: CustomFillTime
)(implicit s: Scheduler)
    extends CirceBuyerSettingsInstances
    with CirceAnalyticsInstances {

  private val noTrackNoticeUrlsForDebugEnv = configuration.get[Boolean]("tracking.notice.urls.nottrack")
  private val defaultSegmentExternalId     = configuration.get[Long]("tracking.banner.default.external.segment.id")
  private val defaultPlacementExternalId   = configuration.get[Long]("tracking.banner.default.external.placement.id")
  private val appTaskId                    = configuration.get[String]("application-task-id")
  private val ttl                          = configuration.get[Long]("tracking.ttl.seconds") seconds

  Logger.debug("TRACKING DURATION CACHE in seconds: " + ttl.toSeconds)

  private def cacheKey(entity: String, id: String, adType: String, ifa: String) = s"track#$entity#$id#$adType#$ifa#v2"

  /**
   * Track event
   *
   * @param request http request
   * @param event   request event: Impression, Click, Finish, Fill
   */
  def track(event: TrackingEventType)(implicit request: RequestHeader) = {
    import CallbackParams._

    val currentDateTime = DateTime.now.withZone(DateTimeZone.UTC)

    val contextDecoder: UrlStateDecoder[CallbackContext] = implicitly[UrlStateDecoder[CallbackContext]]
    val trackersDecoder: UrlStateDecoder[List[Uri]]      = implicitly[UrlStateDecoder[List[Uri]]]
    val singleUrlDecoder: UrlStateDecoder[Uri]           = implicitly[UrlStateDecoder[Uri]]

    val trackingMethod =
      request
        .getQueryString(TrackingMethodParam)
        .flatMap(CallbackTrackingMethod.withValueOpt)
        .getOrElse(CallbackTrackingMethod.Unknown)

    val tokenOpt      = request.getQueryString(TokenParam)
    val contextOpt    = request.getQueryString(ContextParam).flatMap(contextDecoder(_).toOption)
    val trackers      = request.getQueryString(TrackersParam).flatMap(trackersDecoder(_).toOption).getOrElse(Nil)
    val nurlOpt       = request.getQueryString(NurlParam).flatMap(singleUrlDecoder(_).toOption)
    val burlOpt       = request.getQueryString(BurlParam).flatMap(singleUrlDecoder(_).toOption)
    val nurlDomainOpt = nurlOpt.map(u => u.authority.host.toString)

    nurlOpt.foreach(n => Logger.debug(s"NURL in TRACKING: $n"))

    val segmentId = Try(
      request
        .getQueryString(SegmentIdParam)
        .map(id => id.toLong)
        .get
    ).toOption.getOrElse(defaultSegmentExternalId)

    val placementId = Try(
      request
        .getQueryString(CallbackParams.PlacementIdParam)
        .map(id => id.toLong)
        .get
    ).toOption.getOrElse(defaultPlacementExternalId)

    val key = cacheKey(
      event.value,
      contextOpt.map(_.bidRequestId).getOrElse("no_bid_request_id"),
      contextOpt.flatMap(_.adType.map(_.entryName)).getOrElse("not_ad_type"),
      contextOpt.flatMap(_.ifa).getOrElse("no_ifa")
    )
    val value = appTaskId

    Logger.debug(s"############### Key: $key #### Value: $value ###############")

    def processingTrack(trackable: Boolean): Task[Unit] = {

      import com.appodealx.exchange.common.models.analytics.InvalidEventStatus._

      def executeNoticeUrl(urlOpt: Option[Uri], urlName: String) =
        OptionT(Future(urlOpt)).semiflatMap { url =>
          if (noTrackNoticeUrlsForDebugEnv) {
            Logger.warn(s" Skipping track ${urlName.toUpperCase}: $url")
            Future.successful("exception_skipped_by_debug_flag")
          } else {
            Logger.debug(s">> Request $urlName: ${url.toString}")
            ws.url(url.toString)
              .withRequestTimeout(1 second)
              .get()
              .map(res => s"http_status_${res.status}")
              .recover {
                case e: Exception => s"exception_${e.getClass.getSimpleName.toLowerCase}"
              }
          }
        }.value

      // Handling empty or invalid required parameters
      if (contextOpt.isDefined
          && tokenOpt.isDefined
          && trackable) {

        val context = contextOpt.get

        if (contextOpt.isEmpty) Logger.error("ContextInfo for tracking not defined!")

        val secureParams = request.queryString.flatMap {
          case (k, seq) =>
            seq.headOption.map(k -> _)
        }

        val tokenIsEqual = signService.verify(secureParams)

        val trackIsAlive = Math.abs(Seconds.secondsBetween(currentDateTime, context.timestamp).getSeconds) <= ttl.toSeconds

        // Check parameters is actual and timestampAuction is not so old (e.g. 24 hours)
        if (tokenIsEqual) {
          if (trackIsAlive) {

            trackers.foreach { trUri =>
              Logger.debug(s">> Request tracking url: ${trUri.toString}")
              ws.url(trUri.toString)
                .withRequestTimeout(1 second)
                .get()
                .onComplete {
                  case Success(_) => ()
                  case Failure(exception) =>
                    Logger.error(exception.getMessage, exception)
                    Logger.debug(
                      s"Tracking request to `${trUri.authority.host.toString}` " +
                        s"with agency `${context.agencyName.getOrElse("unknown")}` " +
                        s"and bidder `${context.bidderName.getOrElse("unknown")}` " +
                        s"failed with exception: $exception"
                    )
                }
            }

            val nurlStatus = executeNoticeUrl(nurlOpt, "nurl")
            nurlStatus.map(_.foreach(status => Logger.debug(s"nurl request status: $status")))

            val burlStatus = executeNoticeUrl(burlOpt, "burl")
            burlStatus.map(_.foreach(status => Logger.debug(s"burl request status: $status")))

            latency(context, event, currentDateTime).flatMap { latency =>
              {
                for {
                  status <- Task.fromFuture(nurlStatus).onErrorRecover {
                             case th => Some(s"exception_${th.getClass.getSimpleName.toLowerCase}")
                           }
                  _ <- emitterService.send(
                        timestamp = currentDateTime,
                        timestampAuction = context.timestamp,
                        clearPrice = context.clearingPrice,
                        bidRequestId = context.bidRequestId,
                        context = contextOpt,
                        nurlDomain = nurlDomainOpt,
                        nurlResponseStatus = status,
                        externalSegmentId = segmentId,
                        externalPlacementId = placementId,
                        event = event,
                        trackingMethod = trackingMethod,
                        latency = latency
                      )
                } yield ()
              }.forkAndForget
            }
          } else {
            Logger.warn(s"Request to ${request.path} expired with context: ${context.asJson
              .pretty(Printer.noSpaces.copy(dropNullValues = true))}")
            emitterService.sendNonValidEvents(
              currentDateTime,
              event,
              EXPIRED,
              trackingMethod,
              contextOpt,
              segmentId.some,
              placementId.some
            )
          }
        } else {
          Logger.debug(s"Request to ${request.path} with incorrect token `${tokenOpt
            .getOrElse("EMPTY")}` for context: ${context.asJson.pretty(Printer.noSpaces.copy(dropNullValues = true))}")
          emitterService.sendNonValidEvents(
            currentDateTime,
            event,
            INVALID_TOKEN,
            trackingMethod,
            contextOpt,
            segmentId.some,
            placementId.some
          )
        }
      } else {
        if (!trackable) {
          Logger.warn(s"Request to ${request.path} is duplicate event with query params: ${request.queryString.asJson
            .pretty(Printer.noSpaces.copy(dropNullValues = true))}")
          emitterService.sendNonValidEvents(
            currentDateTime,
            event,
            DUPLICATE,
            trackingMethod,
            contextOpt,
            segmentId.some,
            placementId.some
          )
        } else {
          val contextEmpty = if (contextOpt.isEmpty) "no context" else " provided context"
          val tokenEmpty   = if (tokenOpt.isEmpty) "no token" else " provided token"

          Logger.debug(s"Request rejected with $contextEmpty and $tokenEmpty by url: ${request.uri}")

          val result =
            (contextOpt, tokenOpt) match {
              case (None, None) =>
                emitterService.sendNonValidEvents(
                  currentDateTime,
                  event,
                  NO_CONTEXT_AND_TOKEN,
                  trackingMethod,
                  contextOpt,
                  segmentId.some,
                  placementId.some
                )
              case (None, _) =>
                emitterService.sendNonValidEvents(
                  currentDateTime,
                  event,
                  NO_CONTEXT,
                  trackingMethod,
                  contextOpt,
                  segmentId.some,
                  placementId.some
                )
              case (_, None) =>
                emitterService.sendNonValidEvents(
                  currentDateTime,
                  event,
                  NO_TOKEN,
                  trackingMethod,
                  contextOpt,
                  segmentId.some,
                  placementId.some
                )
              case _ => Task.now(())
            }

          result
        }
      }
    }

    deDuplicatingChecker.verify(key, value, ttl).flatMap(processingTrack).runToFuture
  }

  private def latency(
    context: CallbackContext,
    eventType: TrackingEventType,
    currentTime: DateTime,
    ttl: FiniteDuration = 2 minutes
  ): Task[Option[Double]] = {
    def cacheKey(id: String, adType: String) = s"latency#$id#$adType#v1"

    val customKey = cacheKey(
      context.bidRequestId,
      context.adType.map(_.entryName).getOrElse("no_ad_type")
    )

    eventType match {
      case TrackingEventType.CustomEvent =>
        customFillTime.push(customKey, currentTime.getMillis, ttl).map(_ => None)

      case TrackingEventType.FillEvent =>
        customFillTime.pull(customKey).map(_.map(t => (currentTime.getMillis - t) / 1000.0))
      case _ => Task.pure(None)
    }
  }
}
