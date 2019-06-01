package services.auction.rtb3

import java.util.UUID

import com.appodealx.exchange.common.models.{HtmlMarkup, NativeMarkup, Size, Uri, VastMarkup, XmlMarkup}
import com.appodealx.openrtb.native.response.Native
import io.bidmachine.protobuf
import io.bidmachine.protobuf.{AdExtension, EventTypeExtended}
import io.bidmachine.protobuf.EventTypeExtended.{EVENT_TYPE_EXTENDED_CLICK, EVENT_TYPE_EXTENDED_CLOSED, EVENT_TYPE_EXTENDED_DESTROYED, EVENT_TYPE_EXTENDED_ERROR, EVENT_TYPE_EXTENDED_IMPRESSION, EVENT_TYPE_EXTENDED_LOADED, EVENT_TYPE_EXTENDED_TRACKING_ERROR, EVENT_TYPE_EXTENDED_VIEWABLE}
import io.bidmachine.protobuf.adcom.Ad.{Display, Event, Video}
import io.bidmachine.protobuf.adcom.ApiFramework.API_FRAMEWORK_MRAID_3_0
import io.bidmachine.protobuf.adcom.DisplayCreativeType.{DISPLAY_CREATIVE_TYPE_HTML, DISPLAY_CREATIVE_TYPE_NATIVE_OBJECT}
import io.bidmachine.protobuf.adcom.EventType.EVENT_TYPE_IMPRESSION
import io.bidmachine.protobuf.adcom.{EventTrackingMethod, EventType, VideoCreativeType, Ad => AdcomAd}
import io.bidmachine.protobuf.openrtb.Openrtb
import io.bidmachine.protobuf.openrtb.Openrtb.Payload.Response
import io.bidmachine.protobuf.openrtb.Response.Seatbid
import io.bidmachine.protobuf.openrtb.Response.Seatbid.Bid
import models.{Ad, DefaultWriteables}
import models.rtb3.Rtb3RequestInfo
import play.api.http.Writeable
import services.BidMachineSettings
import services.auction.rtb3.Rtb3BidResponseMapper.{AdcomAdMapper, TimeThreshold, UrlPrefix}

import cats.syntax.option._

trait Rtb3BidResponseMapper {
  def mapToRtb3Response(reqInfo: Rtb3RequestInfo)(ad: models.Ad): Option[Openrtb]
}

object Rtb3BidResponseMapper {
  type TimeThreshold = Int
  type UrlPrefix     = String
  type AdcomAdMapper   = (Ad, UrlPrefix, TimeThreshold) => Option[AdcomAd]
}

class Rtb3BidResponseMapperImpl(mapToAdcomAd: AdcomAdMapper, settings: BidMachineSettings) extends Rtb3BidResponseMapper {
  private val urlPrefix = "bidmachine.io/"

  override def mapToRtb3Response(reqInfo: Rtb3RequestInfo)(ad: Ad) = {

    def toOpenrtbResponse(adcomAd: AdcomAd) = {
      val media  = com.google.protobuf.any.Any.pack(adcomAd, urlPrefix)

      val bid = Bid(
        id = UUID.randomUUID.toString,
        item = reqInfo.itemId.value,
        media = media.some,
        price = ad.sspIncome,
        deal = reqInfo.dealId.value,
        cid = ad.metadata.`X-Appodeal-Campaign-ID`.getOrElse(""),
        exp = settings.expirationTime
      )

      val seatBid = Seatbid(seat = ad.metadata.`X-Appodeal-Demand-Source`.getOrElse(""), bid = List(bid))
      val reqId   = if(reqInfo.reqId.value.nonEmpty) reqInfo.reqId.value else java.util.UUID.randomUUID().toString
      val res     = protobuf.openrtb.Response(id = reqId, seatbid = List(seatBid))

      Openrtb(ver = "3.0", domainspec = "adcom", domainver = "1.0", payload = Response(res))

    }

    mapToAdcomAd(ad, urlPrefix, settings.viewabilityTimeThreshold) map toOpenrtbResponse
  }
}

object AdcomAdMapperImpl extends AdcomAdMapper with DefaultWriteables {
  override def apply(ad: Ad, prefix: UrlPrefix, viewabilityTimeThreshold: TimeThreshold): Option[AdcomAd] = {
    def render[M: Writeable](m: M) = implicitly[Writeable[M]].transform(m).utf8String

    ad.markup match {
      case HtmlMarkup(markup) => bannerAd(ad, render(markup), prefix).some
      case XmlMarkup(markup)  => videoAd(ad, render(markup), prefix).some
      case VastMarkup(markup) => videoAd(ad, render(markup), prefix).some
      case NativeMarkup(n)    => nativeAd(ad, n, prefix, viewabilityTimeThreshold).some
      case _                  => None
    }
  }

  private def videoAd(ad: Ad, body: String, prefix: UrlPrefix) = {
    val video = Video(
      `type` = List(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_3_0),
      adm = body
    )

    AdcomAd(
      id = ad.metadata.`X-Appodeal-Creative-ID`.getOrElse(""),
      video = video.some,
      adomain = ad.adomain.getOrElse(Nil),
      bundle = List(ad.bundle.getOrElse("")),
      cat = ad.cat.getOrElse(Nil),
      ext = List(com.google.protobuf.any.Any.pack(createAdExtension(ad), prefix))
    )
  }

  private def bannerAd(ad: Ad, body: String, prefix: UrlPrefix) = {
    val size = ad.size.getOrElse(Size(0, 0))

    val display = Display(
      api = List(API_FRAMEWORK_MRAID_3_0),
      `type` = List(DISPLAY_CREATIVE_TYPE_HTML),
      w = size.width,
      h = size.height,
      adm = body
    )

    AdcomAd(
      id = ad.metadata.`X-Appodeal-Creative-ID`.getOrElse(""),
      display = display.some,
      adomain = ad.adomain.getOrElse(Nil),
      bundle = List(ad.bundle.getOrElse("")),
      cat = ad.cat.getOrElse(Nil),
      ext = List(com.google.protobuf.any.Any.pack(createAdExtension(ad), prefix))
    )
  }

  private def nativeAd(ad: Ad, n: Native, prefix: UrlPrefix, viewabilityTimeThreshold: TimeThreshold) = {

    val events = n.imptrackers.map { trackers =>
      trackers.map(
        url =>
          Event(
            `type` = EVENT_TYPE_IMPRESSION,
            url = url,
          )
      )
    }.getOrElse(Nil)

    val display = Display(
      `type` = List(DISPLAY_CREATIVE_TYPE_NATIVE_OBJECT),
      native = n.toVersion3.some,
      event = events
    )

    val extension = createAdExtension(ad).copy(viewabilityDurationThreshold = viewabilityTimeThreshold)

    AdcomAd(
      id = ad.metadata.`X-Appodeal-Creative-ID`.getOrElse(""),
      display = display.some,
      adomain = ad.adomain.getOrElse(Nil),
      bundle = List(ad.bundle.getOrElse("")),
      cat = ad.cat.getOrElse(Nil),
      ext = List(com.google.protobuf.any.Any.pack(extension, prefix))
    )
  }

  private def createAdExtension(ad: Ad) = {
    val preload   = ad.metadata.`X-Appodeal-Cache`
    val skipAfter = ad.metadata.`X-Appodeal-Close-Time`
    val event     = eventTracker(ad)

    AdExtension(preload = preload, skipAfter = skipAfter, event = event)
  }

  private def eventTracker(ad: Ad) = {

    def event(eventType: EventTypeExtended, uri: Uri) =
      Event(`type` = EventType.fromValue(eventType.value),
        method = EventTrackingMethod.EVENT_TRACKING_METHOD_IMAGE_PIXEL,
        url = uri.toString())

    val loaded        = ad.trackingEvents.loaded.map(event(EVENT_TYPE_EXTENDED_LOADED, _))
    val impression    = ad.trackingEvents.impression.map(event(EVENT_TYPE_EXTENDED_IMPRESSION, _))
    val click         = ad.trackingEvents.click.map(event(EVENT_TYPE_EXTENDED_CLICK, _))
    val closed        = ad.trackingEvents.closed.map(event(EVENT_TYPE_EXTENDED_CLOSED, _))
    val error         = ad.trackingEvents.error.map(event(EVENT_TYPE_EXTENDED_ERROR, _))
    val trackingError = ad.trackingEvents.trackingError.map(event(EVENT_TYPE_EXTENDED_TRACKING_ERROR, _))
    val destroyed     = ad.trackingEvents.destroy.map(event(EVENT_TYPE_EXTENDED_DESTROYED, _))
    val viewable      = ad.trackingEvents.viewable.map(event(EVENT_TYPE_EXTENDED_VIEWABLE, _))

    List(loaded, impression, click, closed, destroyed, viewable, error, trackingError).flatten
  }
}