package services.callback.injectors.nast

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.auction.Adm.NAST
import com.appodealx.openrtb.native.response.Native
import models.RequestHost
import services.callback.builders._
import services.callback.injectors.CallbackInjectorInst

import cats.syntax.option._

class NativeCallbackInjectorImpl(impBuilder: ImpressionCallbackBuilder,
                                 clickBuilder: ClickCallbackBuilder,
                                 finishBuilder: ClosedCallbackBuilder,
                                 fillBuilder: LoadedCallbackBuilder,
                                 destroyedBuilder: DestroyedCallbackBuilder,
                                 viewableBuilder: ViewableCallbackBuilder,
                                 errorBuilder: ErrorCallbackBuilder)
    extends CallbackInjectorInst[Native]
    with NativeCallbackInjector {

  override def impressionCallbackBuilder: CallbackBuilder = impBuilder

  override def clickCallbackBuilder: CallbackBuilder = clickBuilder

  override def loadedCallbackBuilder: CallbackBuilder = fillBuilder

  override def closedCallbackBuilder: CallbackBuilder = finishBuilder

  override def injectMarkup(adMarkup: Native, context: CallbackContext, trackers: ContextTrackers, errorsOnly: Boolean)(
    implicit requestHost: RequestHost
  ) =
    injectNast(adMarkup, context, trackers)

  override def mkEvents(context: CallbackContext, trackers: ContextTrackers)(implicit requestHost: RequestHost) =
    AuctionTrackingEvents(
      click = clickBuilder.build(context, trackers.clickTrackers, CallbackTrackingMethod.Header).some,
      impression = impBuilder
        .build(context, trackers.impTrackers, CallbackTrackingMethod.Header, nurl = trackers.nurl, burl = trackers.burl)
        .some,
      closed = finishBuilder.build(context, Nil, CallbackTrackingMethod.Header).some,
      loaded = fillBuilder.build(context, Nil, CallbackTrackingMethod.Header).some,
      error = errorBuilder.buildErrorNast(context).some,
      trackingError = errorBuilder.buildTrackingError(context).some,
      destroy = destroyedBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some,
      viewable = viewableBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some
    )

  override def injectThirdPartyMarkup(adMarkup: Native, context: CallbackContext) = adMarkup
}
