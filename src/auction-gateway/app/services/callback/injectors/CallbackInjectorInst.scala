package services.callback.injectors

import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.auction.Adm
import com.appodealx.exchange.common.utils.TypeClassInst
import models.RequestHost
import models.auction.Metadata

abstract class CallbackInjectorInst[A: Adm] extends TypeClassInst[A] {

  def injectMarkup(adMarkup: A,
                   context: CallbackContext,
                   trackers: ContextTrackers, // Used only for native
                   errorsOnly: Boolean = false)(implicit requestHost: RequestHost): A

  def injectThirdPartyMarkup(adMarkup: A, context: CallbackContext): A

  def mkMetadata(metadata: Metadata, context: CallbackContext, trackingEvents: AuctionTrackingEvents)(
    implicit requestHost: RequestHost
  ): Metadata = metadata

  def mkEvents(context: CallbackContext, trackers: ContextTrackers)(
    implicit requestHost: RequestHost
  ): AuctionTrackingEvents

  def injectLoadedPixelEvent(markup: A, context: CallbackContext)(implicit requestHost: RequestHost): A = markup
}
