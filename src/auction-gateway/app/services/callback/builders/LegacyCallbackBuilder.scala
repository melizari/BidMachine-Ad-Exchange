package services.callback.builders

import com.appodealx.exchange.common.models.analytics.{CallbackContext, UrlStateEncoder}
import com.appodealx.exchange.common.models.circe.CirceAnalyticsInstances
import com.appodealx.exchange.common.models.{CallbackMacros, CallbackTrackingMethod, Platform, Uri}
import io.lemonlabs.uri.config.UriConfig
import io.lemonlabs.uri.{AbsoluteUrl, Authority, QueryString, UrlPath}
import models.RequestHost
import play.api.mvc.Call
import play.api.{Configuration, Logger}

abstract class LegacyCallbackBuilder(call: String => Call, configuration: Configuration)(implicit uriConfig: UriConfig)
    extends CallbackBuilder with CirceAnalyticsInstances {

  private val logger = Logger(getClass)

  private val secureAndroid = configuration.get[Boolean]("settings.default.android-callback-secure-scheme")

  def secure(context: CallbackContext) =
    !context.deviceOs.contains(Platform.Android.entryName) || secureAndroid

  def build(context: CallbackContext,
            trackers: List[String],
            trackingMethod: CallbackTrackingMethod,
            nurl: Option[String] = None,
            burl: Option[String] = None,
            escapeMacros: Boolean = false,
            metadata: Boolean = false)(implicit requestHost: RequestHost): Uri = {

    val contextEncoder  = implicitly[UrlStateEncoder[CallbackContext]]
    val trackersEncoder = implicitly[UrlStateEncoder[List[String]]]
    val urlEncoder      = implicitly[UrlStateEncoder[String]]

    val nurlEncoded = nurl.map(urlEncoder)
    val burlEncoded = burl.map(urlEncoder)

    nurl.foreach { n =>
      logger.debug(s"NURL: $n")
      logger.debug(s"Encoded NURL: ${nurlEncoded.getOrElse("")}")
    }

    import com.appodealx.exchange.common.models.CallbackMacros._
    import com.appodealx.exchange.common.models.CallbackParams._
    import com.appodealx.exchange.common.utils.StringWithMacroDecorator

    val params = Map(
      TrackingMethodParam -> trackingMethod.value.toString,
      ContextParam        -> contextEncoder(context),
      TrackersParam       -> trackersEncoder(trackers),
    ) ++
      nurlEncoded.map(NurlParam -> _) ++
      burlEncoded.map(BurlParam -> _)

    val macrosParams =
      if (metadata) {
        Map(SegmentIdParam -> CallbackMacros.SegmentIdMacros, PlacementIdParam -> CallbackMacros.PlacementIdMacros)
      } else {
        Map(SegmentIdParam -> SegmentId.asMacro(escapeMacros), PlacementIdParam -> PlacementId.asMacro(escapeMacros))
      }

    val signedParams = paramsSigner.sign(params ++ macrosParams)
    val queryParams  = signedParams.mapValues(Option(_)).toVector

    val uri = AbsoluteUrl(
      scheme = scheme(context),
      authority = Authority(requestHost.host),
      path = UrlPath.parse(call(context.adType.map(_.entryName).getOrElse("unknown_type")).url).toAbsoluteOrEmpty,
      query = QueryString(queryParams),
      fragment = None
    )

    logger.debug(s"Callback url: ${uri.toString()}")

    uri
  }
}
