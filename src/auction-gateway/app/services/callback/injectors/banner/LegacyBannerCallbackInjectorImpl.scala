package services.callback.injectors.banner

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.auction.Adm.MRAID
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import models.RequestHost
import models.auction.Metadata
import play.api.Configuration
import play.twirl.api.Html
import services.auction.Headers._
import services.callback.builders._
import services.callback.injectors.CallbackInjectorInst
import services.callback.markup.banner.BannerMarkups.pixelTracker
import services.callback.markup.banner.HtmlMarkupBuilder

import cats.syntax.option._

class LegacyBannerCallbackInjectorImpl(impBuilder: LegacyImpressionCallbackBuilder,
                                       clickBuilder: LegacyClickCallbackBuilder,
                                       finishBuilder: LegacyFinishCallbackBuilder,
                                       fillBuilder: LegacyFillsCallbackBuilder,
                                       customEventBuilder: CustomEventLegacyCallbackBuilder,
                                       errorBuilder: LegacyErrorCallbackBuilder,
                                       configuration: Configuration,
                                       val thirdPartyMarkupBuilders: List[HtmlMarkupBuilder])
    extends CallbackInjectorInst[Html]
    with CirceModelsInstances
    with BannerCallbackInjector {

  private val loadedPixelEnabled = configuration.get[Boolean]("settings.default.loaded-event-pixel-enabled")

  override def impressionCallbackBuilder = impBuilder

  override def clickCallbackBuilder = clickBuilder

  override def loadedCallbackBuilder = fillBuilder

  override def customEventCallbackBuilder = customEventBuilder

  override def closedCallbackBuilder = finishBuilder

  override def injectMarkup(adMarkup: Html, context: CallbackContext, trackers: ContextTrackers, errorsOnly: Boolean)(
    implicit requestHost: RequestHost
  ) =
    buildMarkup(adMarkup.body, context, trackers)

  override def injectThirdPartyMarkup(adMarkup: Html, context: CallbackContext) = thirdPartyMarkup(adMarkup, context)

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
      error = errorBuilder.buildErrorMraid(context).some
    )

  override def injectLoadedPixelEvent(markup: Html, context: CallbackContext)(implicit requestHost: RequestHost) =
    if (loadedPixelEnabled && !context.adNetwork.getOrElse(false)) {
      val loadedUri = customEventCallbackBuilder.build(context, Nil, CallbackTrackingMethod.BannerPixel)
      Html(pixelTracker(loadedUri) + markup.body)
    } else {
      markup
    }
}
