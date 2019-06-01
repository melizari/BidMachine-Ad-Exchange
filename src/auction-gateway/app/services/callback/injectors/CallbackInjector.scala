package services.callback.injectors

import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.auction.Adm
import com.appodealx.exchange.common.utils.TypeClassSelector
import models.RequestHost
import models.auction.Metadata

abstract class CallbackInjector(injectors: CallbackInjectorInst[_]*)
    extends TypeClassSelector[CallbackInjectorInst](injectors) {

  def injectMarkup[A: Adm](markup: A, context: CallbackContext, trackers: ContextTrackers, errorsOnly: Boolean = false)(
    implicit requestHost: RequestHost
  ): A =
    selectInst[A].injectMarkup(markup, context, trackers, errorsOnly)

  def injectThirdPartyMarkup[A: Adm](markup: A, context: CallbackContext): A =
    selectInst[A].injectThirdPartyMarkup(markup, context)

  def mkMetadata[A: Adm](metadata: Metadata, context: CallbackContext, trackingEvents: AuctionTrackingEvents)(
    implicit requestHost: RequestHost
  ): Metadata =
    selectInst[A].mkMetadata(metadata, context, trackingEvents)

  def events[A: Adm](context: CallbackContext, trackers: ContextTrackers)(
    implicit requestHost: RequestHost
  ): AuctionTrackingEvents =
    selectInst[A].mkEvents(context, trackers)

  def injectLoadedEventPixel[A: Adm](markup: A, context: CallbackContext)(implicit rh: RequestHost): A =
    selectInst[A].injectLoadedPixelEvent(markup, context)
}
