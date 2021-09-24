package controllers.auction.renderers

import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.openrtb.{Bid, BidResponse, SeatBid}
import io.circe.{Printer}
import models.{Ad, DefaultWriteables}
import play.api.libs.circe.Circe
import play.api.libs.json._
import play.api.mvc.{Result, Results}

import java.util.UUID

trait RtbAdMarkupRendering extends DefaultWriteables with Results with Circe {

  private implicit val bidResponseWrites = Json.writes[BidResponse]
  private implicit val seatbidWrites = Json.writes[SeatBid]
  private implicit val bidWrites = Json.writes[Bid]

  private implicit val customPrinter = Printer.noSpaces.copy(dropNullValues = true)

  def renderAd[P: Plc](ad: Ad): Result = {

    val bid = Bid(
      id = ad.metadata.`X-Appodeal-Bid-Request-ID`,
      impid = ad.metadata.`X-Appodeal-Impression-ID`.get,
      price = ad.sspIncome,
      adid = Option(ad.metadata.`X-Appodeal-Bid-Request-ID`),
      nurl = ad.nurl,
      burl = ad.trackingEvents.viewable.map(_.toString()),
      adm = Option(ad.markup.toString),
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

    val json = Json.toJson(bidResponse)

    Ok(json).withHeaders(DefaultHeaderRenderer.renderHeaders(ad.metadata): _*)
  }
}
