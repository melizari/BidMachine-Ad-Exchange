package services.callback.injectors.vast

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.exchange.common.models.auction.Adm.Vast
import models.RequestHost
import services.callback.builders._
import services.callback.injectors.CallbackInjectorInst
import services.callback.markup.video.VastMarkupBuilder

import cats.syntax.option._

class VideoCallbackInjectorImpl(impBuilder: ImpressionCallbackBuilder,
                                clickBuilder: ClickCallbackBuilder,
                                closedBuilder: ClosedCallbackBuilder,
                                loadedBuilder: LoadedCallbackBuilder,
                                destroyedBuilder: DestroyedCallbackBuilder,
                                viewableBuilder: ViewableCallbackBuilder,
                                errorBuilder: ErrorCallbackBuilder,
                                val thirdPartyMarkupBuilders: List[VastMarkupBuilder])
    extends CallbackInjectorInst[VAST]
    with CirceModelsInstances
    with VideoCallbackInjector {

  override def impressionCallbackBuilder: CallbackBuilder = impBuilder

  override def closedCallbackBuilder: CallbackBuilder = closedBuilder

  override def errorCallbackBuilder: ErrorCallbackBuilderLike = errorBuilder

  override def injectMarkup(adMarkup: VAST, context: CallbackContext, trackers: ContextTrackers, errorsOnly: Boolean)(
    implicit requestHost: RequestHost
  ): VAST =
    vast(adMarkup, context, trackers, errorsOnly)

  override def mkEvents(context: CallbackContext, trackers: ContextTrackers)(implicit requestHost: RequestHost) =
    AuctionTrackingEvents(
      click = clickBuilder.build(context, trackers.clickTrackers, CallbackTrackingMethod.Header, metadata = true).some,
      impression = impBuilder
        .build(context,
               trackers.impTrackers,
               CallbackTrackingMethod.Header,
               nurl = trackers.nurl,
               burl = trackers.burl,
               metadata = true)
        .some,
      closed = closedBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some,
      loaded = loadedBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some,
      error = errorBuilder.buildErrorVast(context).some,
      trackingError = errorBuilder.buildTrackingError(context).some,
      destroy = destroyedBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some,
      viewable = viewableBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some
    )

  override def injectThirdPartyMarkup(adMarkup: VAST, context: CallbackContext) = injectThirdParty(adMarkup, context)
}
