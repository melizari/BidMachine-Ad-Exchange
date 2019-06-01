package services

import cats.syntax.either._
import com.appodealx.exchange.common.models.analytics.{ErrorContext, KafkaError, TrackingEventType, UrlStateDecoder}
import com.appodealx.exchange.common.models.circe.CirceAnalyticsInstances
import com.appodealx.exchange.common.models.{CallbackParams, Failure, FailureReason}
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import monix.eval.Task
import monix.execution.Scheduler
import play.api.mvc.RequestHeader

import scala.concurrent.ExecutionContext


class AdErrorTrackerService(emitterService: EmitterService,
                            dcSettings: DatacenterMetadataSettings)(implicit ec: ExecutionContext)
  extends CirceBuyerSettingsInstances
    with CirceAnalyticsInstances {

  private val contextDecoder: UrlStateDecoder[ErrorContext] = implicitly[UrlStateDecoder[ErrorContext]]

  def trackError(event: TrackingEventType)(implicit request: RequestHeader, s: Scheduler) = {
    val errorCodeOpt = request.getQueryString(CallbackParams.ErrorParam)
    val ctxOpt: Option[ErrorContext] = request.getQueryString(CallbackParams.ContextParam).flatMap(contextDecoder(_).toOption)
    val kafkaError = {
      for {
        e <- errorCodeOpt
        c <- ctxOpt
      } yield KafkaError(
        timestamp = c.timestamp,
        errorCode = Some(e),
        appId = c.appId,
        appIdRaw = c.appIdRaw,
        appName = c.appName,
        appBundle = c.appBundle,
        agencyExternalId = c.extAgencyId,
        agencyId = c.agencyId,
        agencyName = c.agencyName,
        deviceOs = c.deviceOs,
        deviceOsVersion = c.deviceOsVersion,
        deviceIfa = c.deviceIfa,
        sdkName = c.sdkName,
        sdkVersion = c.sdkVersion,
        displayManager = c.displayManager,
        displayManagerVersion = c.displayManagerVersion,
        cid = c.cid,
        crid = c.crid,
        adType = c.adType,
        creativeType = Some(event.prettyValue),
        country = c.country,
        adNetwork = c.adNetwork,
        adNetworkName = c.adNetworkName,
        adNetworkPlacementId = c.adNetworkPlacementId,
        sellerId = c.sellerId,
        sellerName = c.sellerName,
        gdpr = c.gdpr,
        dcid = Some(dcSettings.dcid)
      )
    }

    kafkaError.fold(Task.now(()))(e => emitterService.sendKafkaError(e)).runToFuture
  }

  def trackEvent(implicit request: RequestHeader, s: Scheduler) = {
    val eventCodeOpt = request.getQueryString(CallbackParams.EventCodeParam)
    val actionCodeOpt = request.getQueryString(CallbackParams.ActionCodeParam)
    val errorReasonOpt = request.getQueryString(CallbackParams.ErrorReasonParam)

    def ctxEmptyFailure[A] = Failure(FailureReason.RequestMissingParametersFailure, s"${CallbackParams.ContextParam} not provided").asLeft[A]

    def decodeContext(string: String) = contextDecoder(string).leftMap(Failure(FailureReason.RequestDecodingFailure, _))

    val ctxOpt =
      request.getQueryString(CallbackParams.ContextParam).map(decodeContext).fold(ctxEmptyFailure[ErrorContext])(identity)

    val kafkaError = {
      for {
        c <- ctxOpt
      } yield {
        KafkaError(
          timestamp = c.timestamp,

          eventCode = eventCodeOpt,
          actionCode = actionCodeOpt,
          errorReason = errorReasonOpt,

          appId = c.appId,
          appIdRaw = c.appIdRaw,
          appName = c.appName,
          appBundle = c.appBundle,
          agencyExternalId = c.extAgencyId,
          agencyId = c.agencyId,
          agencyName = c.agencyName,
          deviceOs = c.deviceOs,
          deviceOsVersion = c.deviceOsVersion,
          deviceIfa = c.deviceIfa,
          sdkName = c.sdkName,
          sdkVersion = c.sdkVersion,
          displayManager = c.displayManager,
          displayManagerVersion = c.displayManagerVersion,
          cid = c.cid,
          crid = c.crid,
          adType = c.adType,
          country = c.country,
          adNetwork = c.adNetwork,
          adNetworkName = c.adNetworkName,
          adNetworkPlacementId = c.adNetworkPlacementId,
          sellerId = c.sellerId,
          sellerName = c.sellerName,
          gdpr = c.gdpr,
          dcid = Some(dcSettings.dcid)
        )
      }
    }

    kafkaError.fold(Task.raiseError, emitterService.sendKafkaError).runToFuture
  }

}
