package services.callback.injectors.nast

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.auction.Adm.NAST
import com.appodealx.openrtb.native.response.Native
import models._
import models.auction.Metadata
import services.auction.Headers._
import services.callback.builders._
import services.callback.injectors.CallbackInjectorInst

import cats.syntax.option._

class LegacyNativeCallbackInjectorImpl(impBuilder: LegacyImpressionCallbackBuilder,
                                       clickBuilder: LegacyClickCallbackBuilder,
                                       finishBuilder: LegacyFinishCallbackBuilder,
                                       fillBuilder: LegacyFillsCallbackBuilder,
                                       errorBuilder: LegacyErrorCallbackBuilder)
    extends CallbackInjectorInst[Native]
    with NativeCallbackInjector {

  override def impressionCallbackBuilder: CallbackBuilder = impBuilder

  override def clickCallbackBuilder: CallbackBuilder = clickBuilder

  override def loadedCallbackBuilder: CallbackBuilder = fillBuilder

  override def closedCallbackBuilder: CallbackBuilder = finishBuilder

  def injectMarkup(adMarkup: Native, context: CallbackContext, trackers: ContextTrackers, errorsOnly: Boolean)(
    implicit requestHost: RequestHost
  ): Native =
    injectNast(adMarkup, context, trackers)

  override def mkMetadata(metadata: Metadata, context: CallbackContext, trackingEvents: AuctionTrackingEvents)(
    implicit requestHost: RequestHost
  ) =
    metadata.copy(
      `X-Appodeal-Url-Click` = trackingEvents.click.map(_.toString),
      `X-Appodeal-Url-Impression` = trackingEvents.impression.map(_.toString),
      `X-Appodeal-Url-Finish` = trackingEvents.closed.map(_.toString),
      `X-Appodeal-Url-Fill` = trackingEvents.loaded.map(_.toString),
      `X-Appodeal-Url-Error` = trackingEvents.error.map(_.toString)
    )

  def mkEvents(context: CallbackContext, trackers: ContextTrackers)(implicit requestHost: RequestHost) =
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
      error = errorBuilder.buildErrorNast(context).some
    )
  override def injectThirdPartyMarkup(adMarkup: Native, context: CallbackContext) = adMarkup
}
