package services.callback.injectors.vast

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.auction.Adm.Vast
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.common.models.rtb.vast._
import models.RequestHost
import models.auction.Metadata
import services.callback.builders._
import services.callback.injectors.CallbackInjectorInst
import services.callback.markup.video.VastMarkupBuilder

import cats.syntax.option._

class LegacyVideoCallbackInjectorImpl(impBuilder: LegacyImpressionCallbackBuilder,
                                      clickBuilder: LegacyClickCallbackBuilder,
                                      finishBuilder: LegacyFinishCallbackBuilder,
                                      fillBuilder: LegacyFillsCallbackBuilder,
                                      errorBuilder: LegacyErrorCallbackBuilder,
                                      val thirdPartyMarkupBuilders: List[VastMarkupBuilder])
    extends CallbackInjectorInst[VAST]
    with CirceModelsInstances
    with VideoCallbackInjector {

  override def impressionCallbackBuilder: CallbackBuilder = impBuilder

  override def closedCallbackBuilder: CallbackBuilder = finishBuilder

  override def errorCallbackBuilder: ErrorCallbackBuilderLike = errorBuilder

  override def injectMarkup(adMarkup: VAST, context: CallbackContext, trackers: ContextTrackers, errorsOnly: Boolean)(
    implicit requestHost: RequestHost
  ): VAST =
    vast(adMarkup, context, trackers, errorsOnly)

  override def mkMetadata(metadata: Metadata, context: CallbackContext, trackingEvents: AuctionTrackingEvents)(
    implicit requestHost: RequestHost
  ) = {

    val error = if (context.adNetwork.exists(identity)) {
      trackingEvents.error.map(_.toString)
    } else {
      None
    }

    metadata.copy(
      `X-Appodeal-Url-Click` = trackingEvents.click.map(_.toString),
      `X-Appodeal-Url-Impression` = trackingEvents.impression.map(_.toString),
      `X-Appodeal-Url-Finish` = trackingEvents.closed.map(_.toString),
      `X-Appodeal-Url-Fill` = trackingEvents.loaded.map(_.toString),
      `X-Appodeal-Url-Error` = error
    )
  }

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
      closed = finishBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some,
      loaded = fillBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some,
      error = if (context.adNetwork.exists(identity)) errorBuilder.buildErrorVast(context).some else None
    )

  override def injectThirdPartyMarkup(adMarkup: VAST, context: CallbackContext): VAST =
    injectThirdParty(adMarkup, context)
}
