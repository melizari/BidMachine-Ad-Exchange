package services.callback.builders

import com.appodealx.exchange.common.models.analytics.CallbackContext
import com.appodealx.exchange.common.models.{CallbackMacros, Platform, Uri}
import com.appodealx.exchange.common.models.CallbackParams._
import controllers.adtracker.routes
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.{AbsoluteUrl, Authority, QueryString, UrlPath}
import models.RequestHost
import play.api.{Configuration, Logger}

class LegacyErrorCallbackBuilder(configuration: Configuration)(implicit uriConfig: UriConfig) extends ErrorCallbackBuilderLike {

  private val logger = Logger(getClass)

  private val secureAndroid = configuration.get[Boolean]("settings.default.android-callback-secure-scheme")

  def secure(context: CallbackContext) =
    !context.deviceOs.contains(Platform.Android.entryName) || secureAndroid

  val UNKNOWN_TYPE = "unknown_type"

  def buildErrorMraid(context: CallbackContext)(implicit requestHost: RequestHost): Uri = {

    val params = Vector(
      ErrorParam -> Option(CallbackMacros.LegacyErrorCodePercent),
      ContextParam -> Option(ctxEncoder(errorContext(context)))
    )

    val uri = AbsoluteUrl(
      scheme = scheme(context),
      authority = Authority(requestHost.host),
      path = UrlPath.parse(routes.AdErrorTrackerController.mraidErrorTyped(context.adType.map(_.entryName).getOrElse(UNKNOWN_TYPE)).url).toAbsoluteOrEmpty,
      query = QueryString(params),
      fragment = None
    )

    logger.debug(s"Error callback url: ${uri.toString()}")

    uri
  }

  def buildErrorNast(context: CallbackContext)(implicit requestHost: RequestHost): Uri = {

    val params = Vector(
      ErrorParam -> Option(CallbackMacros.LegacyErrorCodePercent),
      ContextParam -> Option(ctxEncoder(errorContext(context)))
    )

    val uri = AbsoluteUrl(
      scheme = scheme(context),
      authority = Authority(requestHost.host),
      path = UrlPath.parse(routes.AdErrorTrackerController.nastErrorTyped(context.adType.map(_.entryName).getOrElse(UNKNOWN_TYPE)).url).toAbsoluteOrEmpty,
      query = QueryString(params),
      fragment = None
    )

    logger.debug(s"Error native callback url: ${uri.toString()}")

    uri
  }

  def buildErrorVast(context: CallbackContext, metadata: Boolean = false)(implicit requestHost: RequestHost): Uri = {

    val platform = context.deviceOs.flatMap(Platform.fromString)

    // Error macros for different platforms and metadata flag.
    val errorParam = platform match {
      // for Android and Amazon with metadata headers
      case Some(Platform.Android) | Some(Platform.Amazon) if metadata =>
        ErrorParam -> Option(CallbackMacros.LegacyErrorCodePercent)

      case _ =>
        ErrorParam -> Option(CallbackMacros.LegacyErrorCode)
    }

    val params = Vector(
      errorParam,
      ContextParam -> Option(ctxEncoder(errorContext(context))),
    )

    val uri = AbsoluteUrl(
      scheme = scheme(context),
      authority = Authority(requestHost.host),
      path = UrlPath.parse(routes.AdErrorTrackerController.vastErrorTyped(context.adType.map(_.entryName).getOrElse(UNKNOWN_TYPE)).url).toAbsoluteOrEmpty,
      query = QueryString(params),
      fragment = None
    )

    logger.debug(s"Error video callback url: ${uri.toString()}")

    uri
  }
}
