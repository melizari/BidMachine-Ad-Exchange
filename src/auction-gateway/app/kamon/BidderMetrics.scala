package kamon

import com.appodealx.exchange.common.models.auction.{Bidder, BidderId}
import kamon.metric.StartedTimer


object BidderMetrics {
  private val timer = Kamon.timer("bid_response_time")

  def normalize(name: String) = name.replaceAll("[\\p{Punct}\\s]+", "_").toLowerCase

  def normalizedBidderName(bidder: Bidder) = s"${BidderMetrics.normalize(bidder.title)}_${bidder.id.map(_.value.toString).getOrElse("unknown")}"
  def normalizedBidderName(bidderName: String, bidderId: BidderId) = s"${BidderMetrics.normalize(bidderName)}_${bidderId.value.toString}"

  val bidRequest = Kamon.counter(s"bid_request")
  val bidResponseWin = Kamon.counter(s"bid_response_win")
  val bidResponseLoss = Kamon.counter(s"bid_response_loss")
  val bidResponseNoBid = Kamon.counter(s"bid_response_no_bid")

  def request(adType: String, bidder: String)(country: String) = bidRequest.refine("ad_type" -> adType, "bidder" -> bidder, "country" -> country)
  def win(adType: String, bidder: String)(country: String) = bidResponseWin.refine("ad_type" -> adType, "bidder" -> bidder, "country" -> country)
  def loss(adType: String, bidder: String)(country: String) = bidResponseLoss.refine("ad_type" -> adType, "bidder" -> bidder, "country" -> country)
  def noBid(adType: String, bidder: String)(country: String)(reason: String) =
    bidResponseNoBid.refine("ad_type" -> adType, "bidder" -> bidder, "country" -> country, "reason" -> normalize(reason))

  val BANNER = "banner"
  val INTERSTITIAL = "banner_instl"
  val VIDEO = "video"
  val NATIVE = "native"

  def startTimer(bidder: String): StartedTimer = timer.refine("bidder" -> bidder).start()
}