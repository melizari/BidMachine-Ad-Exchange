package services.callback.builders

import com.appodealx.exchange.common.models.CallbackParams._
import com.appodealx.exchange.common.models.analytics.CallbackContext
import com.appodealx.exchange.common.models.{CallbackMacros, Platform}
import controllers.adtracker.routes
import io.bidmachine.protobuf.EventTypeExtended
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.{AbsoluteUrl, Authority, QueryString, UrlPath}
import models.RequestHost
import play.api.{Configuration, Logger}

class ErrorCallbackBuilder(configuration: Configuration)(implicit uriConfig: UriConfig)
    extends ErrorCallbackBuilderLike {

  private val logger = Logger(getClass)

  import com.appodealx.exchange.common.utils.StringWithMacroDecorator

  private val secureAndroid = configuration.get[Boolean]("settings.default.android-callback-secure-scheme")

  def secure(context: CallbackContext) =
    !context.deviceOs.contains(Platform.Android.entryName) || secureAndroid

  private def build(ctx: CallbackContext, isTrackingError: Boolean = false)(implicit requestHost: RequestHost) = {

    val eventType = if (isTrackingError) {
      EventCodeParam -> EventTypeExtended.EVENT_TYPE_EXTENDED_TRACKING_ERROR.value.toString
    } else {
      EventCodeParam -> EventTypeExtended.EVENT_TYPE_EXTENDED_ERROR.value.toString
    }

    val params = Map(
      eventType,
      ContextParam     -> ctxEncoder(errorContext(ctx)),
      ActionCodeParam  -> CallbackMacros.ActionCodeMacros.asMacro,
      ErrorReasonParam -> CallbackMacros.ErrorReasonMacros.asMacro
    )

    val queryParams = params.mapValues(Option(_)).toVector

    val uri = AbsoluteUrl(
      scheme = scheme(ctx),
      authority = Authority(requestHost.host),
      path = UrlPath.parse(routes.EventTrackerController.event().url).toAbsoluteOrEmpty,
      query = QueryString(queryParams),
      fragment = None
    )

    logger.debug(
      s"${ctx.adType.map(_.prettyValue).getOrElse("UNKNOWN_TYPE")} ${if (isTrackingError) "tracking " else ""}error callback url: ${uri.toString()}"
    )

    uri
  }

  override def buildErrorMraid(context: CallbackContext)(implicit requestHost: RequestHost) = build(context)

  override def buildErrorNast(context: CallbackContext)(implicit requestHost: RequestHost) = build(context)

  override def buildErrorVast(context: CallbackContext, metadata: Boolean = false)(implicit requestHost: RequestHost) =
    build(context)

  override def buildTrackingError(context: CallbackContext, metadata: Boolean = false)(
    implicit requestHost: RequestHost
  ) = build(context, isTrackingError = true)
}
