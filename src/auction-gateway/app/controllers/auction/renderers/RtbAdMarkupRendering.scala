package controllers.auction.renderers

import com.appodealx.exchange.common.models.{HtmlMarkup, NativeMarkup, PbMarkup, VastMarkup, XmlMarkup}
import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.openrtb.{Bid, BidRequest, BidResponse, SeatBid}
import controllers.auction.renderers.DefaultAdMarkupRenderer.Ok
import controllers.auction.renderers.DefaultHeaderRenderer.renderHeaders
import io.circe.{Json, Printer}
import io.circe.syntax.EncoderOps
import models.{Ad, DefaultWriteables}
import play.api.http.Writeable
import play.api.libs.circe.Circe
import play.api.mvc.{Result, Results}

import java.util.UUID

trait RtbAdMarkupRendering extends DefaultWriteables with Results with Circe {

  private implicit val customPrinter = Printer.noSpaces.copy(dropNullValues = true)

  def renderAd[P: Plc](bidRequest: BidRequest)(ad: Ad): Result = {

    def getCreative[A: Writeable](m: A) = {
      implicitly[Writeable[A]].transform(m).utf8String
    }

    val creative = ad.markup match {
      case XmlMarkup(m)    => getCreative(m)
      case HtmlMarkup(m)   => getCreative(m)
      case VastMarkup(m)   => getCreative(m)
      case NativeMarkup(m) => getCreative(m)

      case PbMarkup(j) => j
    }

    val bid = Bid(
      id = bidRequest.id,
      impid = bidRequest.imp.headOption.get.id,
      price = ad.sspIncome,
      adid = Option(ad.metadata.`X-Appodeal-Bid-Request-ID`),
      nurl = ad.trackingEvents.loaded.map(_.toString()),
      burl = ad.trackingEvents.impression.map(_.toString()),
      lurl = ad.trackingEvents.error.map(_.toString()),
      iurl = ad.iurl,
      adm = Option(creative.toString),
      adomain = Option(List(ad.metadata.`X-Appodeal-Adomain`.get)),
      bundle = ad.bundle,
      cid = ad.metadata.`X-Appodeal-Campaign-ID`,
      crid = ad.metadata.`X-Appodeal-Creative-ID`,
      cat = ad.cat,
      h = Option(ad.size.get.height),
      w = Option(ad.size.get.width)
    )

    val seatBid = Option(List(SeatBid(seat = ad.metadata.`X-Appodeal-Demand-Source`, bid = List(bid))))

    val bidResponse = BidResponse(id = UUID.randomUUID.toString, seatbid = seatBid)

    val json = Json.fromJsonObject(bidResponse.asJsonObject)
      /*.add("tracker_loaded", Json.fromString(ad.trackingEvents.loaded.toString))
      .add("tracker_impression", Json.fromString(ad.trackingEvents.impression.toString))
      .add("tracker_click", Json.fromString(ad.trackingEvents.click.toString))
      .add("tracker_closed", Json.fromString(ad.trackingEvents.closed.toString))
      .add("tracker_error", Json.fromString(ad.trackingEvents.error.toString))
      .add("tracker_tracking_error", Json.fromString(ad.trackingEvents.trackingError.toString))
      .add("tracker_destroy", Json.fromString(ad.trackingEvents.destroy.toString))
      .add("tracker_viewable", Json.fromString(ad.trackingEvents.viewable.toString))
    )*/

    Ok(json).withHeaders(DefaultHeaderRenderer.renderHeaders(ad.metadata): _*)
  }
}
