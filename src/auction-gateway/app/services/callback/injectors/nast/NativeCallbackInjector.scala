package services.callback.injectors.nast

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{CallbackContext, ContextTrackers}
import com.appodealx.openrtb.native.response.Native
import io.circe.Json
import models.RequestHost
import services.callback.builders.CallbackBuilder


trait NativeCallbackInjector {

  val FINISH_TRACKERS_KEY = "finishtrackers"

  def impressionCallbackBuilder: CallbackBuilder

  def clickCallbackBuilder: CallbackBuilder

  def loadedCallbackBuilder: CallbackBuilder

  def closedCallbackBuilder: CallbackBuilder

  def injectNast(adMarkup: Native, context: CallbackContext, contextTrackers: ContextTrackers)(implicit requestHost: RequestHost) = {
    val impTrackers = adMarkup.imptrackers.getOrElse(Nil)
    val clickTrackers = adMarkup.link.clicktrackers.getOrElse(Nil)

    val impressionUri = impressionCallbackBuilder.build(context, contextTrackers.impTrackers, CallbackTrackingMethod.Native, contextTrackers.nurl, contextTrackers.burl).toString()
    val clickUri = clickCallbackBuilder.build(context, contextTrackers.clickTrackers, CallbackTrackingMethod.Native).toString()
    val closedUri = closedCallbackBuilder.build(context, Nil, CallbackTrackingMethod.Native).toString()

    val link = adMarkup.link.copy(clicktrackers = Some(clickUri +: clickTrackers))
    val ext = Json.obj(FINISH_TRACKERS_KEY -> Json.arr(Json.fromString(closedUri.toString)))

    adMarkup.copy(
      imptrackers = Some(impressionUri +: impTrackers),
      link = link,
      ext = Some(ext))
  }
}
