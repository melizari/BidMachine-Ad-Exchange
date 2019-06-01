package services.callback.builders

import com.appodealx.exchange.common.models.Uri
import com.appodealx.exchange.common.models.analytics.{CallbackContext, ErrorContext, UrlStateEncoder}
import com.appodealx.exchange.common.models.circe.CirceAnalyticsInstances
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import io.lemonlabs.uri.RelativeUrl
import models.RequestHost
import org.joda.time.DateTime


trait ErrorCallbackBuilderLike extends CirceBuyerSettingsInstances with CirceAnalyticsInstances {

  def secure(context: CallbackContext): Boolean

  def buildErrorMraid(context: CallbackContext)(implicit requestHost: RequestHost): Uri

  def buildErrorNast(context: CallbackContext)(implicit requestHost: RequestHost): Uri

  def buildErrorVast(context: CallbackContext, metadata: Boolean = false)(implicit requestHost: RequestHost): Uri

  protected def errorContext(context: CallbackContext) = ErrorContext(
    timestamp = DateTime.now,
    appId = context.appId.map(_.value),
    appIdRaw = context.appIdRaw,
    appName = context.appName,
    appBundle = context.appBundle,
    extAgencyId = context.externalAgencyId,
    agencyId = context.agencyId,
    agencyName = context.agencyName,
    deviceOs = context.deviceOs,
    deviceOsVersion = context.deviceOsVersion,
    deviceIfa = context.ifa,
    displayManager = context.displayManager,
    displayManagerVersion = context.sdkVersion,
    sdkName = context.sdkName,
    sdkVersion = context.sdkVersion,
    cid = context.cid,
    crid = context.crid,
    adType = context.adType,
    country = context.country,
    adNetwork = context.adNetwork,
    adNetworkName = context.adNetworkName,
    adNetworkPlacementId = context.adNetworkPlacementId,
    sellerId = context.sellerId,
    sellerName = context.sellerName,
    gdpr = context.gdpr
  )

  protected val ctxEncoder: UrlStateEncoder[ErrorContext] = implicitly[UrlStateEncoder[ErrorContext]]

  protected def scheme(context: CallbackContext): String =
    if (secure(context)) "https" else "http"

  def buildTrackingError(context: CallbackContext, metadata: Boolean = false)(implicit requestHost: RequestHost): Uri = RelativeUrl.empty
}
