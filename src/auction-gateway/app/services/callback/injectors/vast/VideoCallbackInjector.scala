package services.callback.injectors.vast

import java.util.UUID

import com.appodealx.exchange.common.models.{CallbackTrackingMethod, Size}
import com.appodealx.exchange.common.models.analytics.{CallbackContext, ContextTrackers}
import com.appodealx.exchange.common.models.rtb.vast.{Ad, Creative, Creatives, Error, Extension, Extensions, Impression, InLine, Linear, MoPubForceOrientation, Tracking, TrackingEvents, VAST, Wrapper}
import models.RequestHost
import services.callback.builders._
import services.callback.markup.video.VastMarkupBuilder

trait VideoCallbackInjector {

  val thirdPartyMarkupBuilders: List[VastMarkupBuilder]

  def impressionCallbackBuilder: CallbackBuilder

  def closedCallbackBuilder: CallbackBuilder

  def errorCallbackBuilder: ErrorCallbackBuilderLike

  def vast(adMarkup: VAST, context: CallbackContext, contextTrackers: ContextTrackers, errorsOnly: Boolean)(
    implicit requestHost: RequestHost
  ) =
    if (errorsOnly) {
      injectError(adMarkup, context)
    } else {
      injectError(injectLoadedEventTrackers(injectImpression(adMarkup, context, contextTrackers), context), context)
    }

  def injectThirdParty(adMarkup: VAST, context: CallbackContext): VAST =
    thirdPartyMarkupBuilders.foldLeft(adMarkup)((m, f) => f(m, context))

  private def dummyCreatives =
    Creatives(List(Creative(id = Some("appodeal-tracking-only-creative"), linear = Some(Linear()))))

  private def injectImpression(markup: VAST, context: CallbackContext, contextTrackers: ContextTrackers)(
    implicit requestHost: RequestHost
  ) = {

    def impressionUri(trackingMethod: CallbackTrackingMethod) =
      impressionCallbackBuilder.build(
        context,
        contextTrackers.impTrackers,
        trackingMethod,
        contextTrackers.nurl,
        contextTrackers.burl
      )

    val impId = UUID.randomUUID.toString

    def injectWrapper(w: Wrapper): Wrapper = {
      val imp = Impression(Some(impId), impressionUri(CallbackTrackingMethod.VastWrapper)) +: w.impression
      w.copy(impression = imp)
    }

    def injectInLine(i: InLine): InLine = {
      val imp = Impression(Some(impId), impressionUri(CallbackTrackingMethod.VastInLine)) +: i.impression
      i.copy(impression = imp)
    }

    def injectAd(ad: Ad): Ad =
      ad.copy(
        inLine = ad.inLine.map(injectInLine),
        wrapper = ad.wrapper.map(injectWrapper)
      )

    val ads = markup.ad.map(_.map(injectAd))
    markup.copy(ad = ads)
  }

  private def injectLoadedEventTrackers(markup: VAST, context: CallbackContext)(implicit requestHost: RequestHost) = {

    def injectExtensions(extensions: Option[Extensions]) = extensions match {
      case Some(Extensions(exts)) => Some(Extensions(customExtensions(context) ::: exts))
      case None =>
        customExtensions(context) match {
          case Nil => None
          case exs => Some(Extensions(exs))
        }
    }

    def finishCallbackUrl(trackingMethod: CallbackTrackingMethod) =
      closedCallbackBuilder.build(context, Nil, trackingMethod)

    def injectTrackingEvents(t: TrackingEvents, trackingMethod: CallbackTrackingMethod) = {
      val events = Tracking(event = "complete", offset = None, value = finishCallbackUrl(trackingMethod).toString) +: t.events
      t.copy(events = events)
    }

    def injectLinear(linear: Linear, trackingMethod: CallbackTrackingMethod) = {
      val events = linear.trackingEvents.getOrElse(TrackingEvents(Nil))
      linear.copy(trackingEvents = Some(injectTrackingEvents(events, trackingMethod)))
    }

    def injectCreative(creative: Creative, trackingMethod: CallbackTrackingMethod) = {
      val linear = creative.linear.map(injectLinear(_, trackingMethod))
      creative.copy(linear = linear)
    }

    def injectCreatives(creatives: Creatives, trackingMethod: CallbackTrackingMethod) = {
      val creativesSeq = creatives.creatives.map(injectCreative(_, trackingMethod))
      creatives.copy(creatives = creativesSeq)
    }

    def injectInLine(inLine: InLine) =
      inLine.copy(
        creatives = injectCreatives(inLine.creatives, CallbackTrackingMethod.VastInLine),
        extensions = injectExtensions(inLine.extensions)
      )

    def injectWrapper(wrapper: Wrapper) = {
      val creatives = wrapper.creatives.getOrElse(dummyCreatives)
      wrapper.copy(
        creatives = Some(injectCreatives(creatives, CallbackTrackingMethod.VastWrapper)),
        extensions = injectExtensions(wrapper.extensions)
      )
    }

    def injectAd(ad: Ad) =
      ad.copy(
        inLine = ad.inLine.map(injectInLine),
        wrapper = ad.wrapper.map(injectWrapper)
      )

    val ads = markup.ad.map(_.map(injectAd))
    markup.copy(ad = ads)
  }

  private def injectError(markup: VAST, context: CallbackContext)(implicit requestHost: RequestHost) = {

    val errors   = markup.error.getOrElse(Nil)
    val errorUrl = errorCallbackBuilder.buildErrorVast(context)
    markup.copy(error = Some(errors :+ Error(errorUrl)))
  }

  private def customExtensions(context: CallbackContext): List[Extension] = {

    def moPubExt =
      context.adSize
        .flatMap(Size.of)
        .filter(s => s.height > s.width)
        .map(_ => MoPubForceOrientation.extension)
        .toList

    moPubExt ::: Nil
  }
}
