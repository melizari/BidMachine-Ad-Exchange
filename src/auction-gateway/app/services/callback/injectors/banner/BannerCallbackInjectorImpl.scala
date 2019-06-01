package services.callback.injectors.banner

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{AuctionTrackingEvents, CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.auction.Adm.MRAID
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import models.RequestHost
import play.api.Configuration
import play.twirl.api.Html
import services.callback.builders._
import services.callback.injectors.CallbackInjectorInst
import services.callback.markup.banner.BannerMarkups.pixelTracker
import services.callback.markup.banner.HtmlMarkupBuilder

import cats.syntax.option._

class BannerCallbackInjectorImpl(impBuilder: ImpressionCallbackBuilder,
                                 clickBuilder: ClickCallbackBuilder,
                                 finishBuilder: ClosedCallbackBuilder,
                                 fillBuilder: LoadedCallbackBuilder,
                                 customEventBuilder: CustomEventCallbackBuilder,
                                 destroyedBuilder: DestroyedCallbackBuilder,
                                 viewableBuilder: ViewableCallbackBuilder,
                                 errorBuilder: ErrorCallbackBuilder,
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
  ) = {

    val rawMarkup = adMarkup.body

    buildMarkup(rawMarkup, context, trackers)
  }

  override def injectThirdPartyMarkup(adMarkup: Html, context: CallbackContext) = thirdPartyMarkup(adMarkup, context)

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
      error = errorBuilder.buildErrorMraid(context).some,
      trackingError = errorBuilder.buildTrackingError(context).some,
      destroy = destroyedBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some,
      viewable = viewableBuilder.build(context, Nil, CallbackTrackingMethod.Header, metadata = true).some
    )

  override def injectLoadedPixelEvent(markup: Html, context: CallbackContext)(implicit requestHost: RequestHost) = {
    if (loadedPixelEnabled && !context.adNetwork.getOrElse(false)) {
      val loadedUri = customEventCallbackBuilder.build(context, Nil, CallbackTrackingMethod.BannerPixel)
      Html(pixelTracker(loadedUri) + markup.body)
    } else {
      markup
    }
  }
}
