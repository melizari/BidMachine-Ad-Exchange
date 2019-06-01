package models.auction

import com.appodealx.openrtb.{CreativeAttribute, QagMediaRating}
import io.circe.Json

case class Bid(price: Double,
               adm: Option[String] = None,
               impid: Option[String] = None,
               adid: Option[String] = None,
               adomain: Option[List[String]] = None,
               bundle: Option[String] = None,
               cat: Option[List[String]] = None,
               attr: Option[List[CreativeAttribute]] = None,
               iurl: Option[String] = None,
               cid: Option[String] = None,
               crid: Option[String] = None,
               qagmediarating: Option[QagMediaRating] = None,
               seatId: Option[String] = None, // For macro subs - OpenRTB: BidResponse.seatbid(N).seat
               bidId: Option[String] = None, // For macro subs - OpenRTB: BidResponse.bidid
               placementId: Option[String] = None,
               adUnit: Option[AdUnit] = None, // Remove
               apiFramework: Option[String] = None,
               customResponse: Option[Json] = None,
               dsp: Option[String] = None, // Remove
               nurl: Option[String] = None,
               burl: Option[String] = None,
               lurl: Option[String] = None,
               ext: Option[Json] = None)
