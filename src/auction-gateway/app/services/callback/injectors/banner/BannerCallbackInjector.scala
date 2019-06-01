package services.callback.injectors.banner

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.{CallbackContext, ContextTrackers}
import models.RequestHost
import play.twirl.api.Html
import services.callback.builders.CallbackBuilder
import services.callback.markup.banner.BannerMarkups._
import services.callback.markup.banner.HtmlMarkupBuilder

trait BannerCallbackInjector {

  val thirdPartyMarkupBuilders: List[HtmlMarkupBuilder]

  def impressionCallbackBuilder: CallbackBuilder

  def clickCallbackBuilder: CallbackBuilder

  def loadedCallbackBuilder: CallbackBuilder

  def closedCallbackBuilder: CallbackBuilder

  def customEventCallbackBuilder: CallbackBuilder

  def buildMarkup(rawMarkup: String, ctx: CallbackContext, ct: ContextTrackers)(
    implicit rh: RequestHost
  ) = {

    val impressionUri =
      impressionCallbackBuilder.build(ctx, ct.impTrackers, CallbackTrackingMethod.BannerJs, ct.nurl, ct.burl)
    val clickUri  = clickCallbackBuilder.build(ctx, ct.clickTrackers, CallbackTrackingMethod.BannerJs)
    val closedUri = closedCallbackBuilder.build(ctx, Nil, CallbackTrackingMethod.BannerJs)
    val loadedUri = loadedCallbackBuilder.build(ctx, Nil, CallbackTrackingMethod.BannerPixel)

    val defaultMarkup = pixelTracker(loadedUri) + rawMarkup + jsScriptForBanner(impressionUri, clickUri, closedUri)
    val adMarkup      = defaultMarkup

    Html(adMarkup)
  }

  def thirdPartyMarkup(adMarkup: Html, context: CallbackContext) =
    Html(thirdPartyMarkupBuilders.foldLeft(adMarkup.body)((m, f) => f(m, context)))
}
