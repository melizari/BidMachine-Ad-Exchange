package services.analytics

import com.appodealx.exchange.common.models.analytics.{AuctionAnalyticEntity, _}
import com.appodealx.exchange.common.models.circe.CirceAnalyticsInstances
import com.appodealx.exchange.common.services.kafka.CirceKafkaProducer
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import kamon.BidderMetrics
import models.auction.NoBidReason
import services.KafkaTopicSettings

import cats.Parallel
import cats.effect.{Concurrent, Sync}
import cats.instances.list._
import cats.syntax.functor._
import cats.syntax.parallel._

import scala.concurrent.ExecutionContext

trait AnalyticsService[F[_]] {

  def send(entities: List[AuctionAnalyticEntity]): F[Unit]

}

class AnalyticsServiceImpl[F[_]: Concurrent: Parallel[?[_], F]](
  kafkaProducer: CirceKafkaProducer,
  kafkaTopicSettings: KafkaTopicSettings
)(implicit ctx: ExecutionContext)
    extends AnalyticsService[F]
    with CirceBuyerSettingsInstances
    with CirceAnalyticsInstances {

  override def send(entities: List[AuctionAnalyticEntity]) = {
    def sendOne(auctionAnalyticEntity: AuctionAnalyticEntity) =
      Sync[F].delay {
        if (auctionAnalyticEntity.adNetwork) {
          sendAdNetworkAnalyticEntityToRtbStats(auctionAnalyticEntity)
          sendAdNetworkStats(auctionAnalyticEntity)
        } else {
          sendRtbCalls(auctionAnalyticEntity)
        }
      }

    entities.parTraverse(sendOne).void
  }

  private def sendRtbCalls(aaes: AuctionAnalyticEntity*) = {
    // Produce metrics for bidder
    val rtbAAEs   = aaes.filterNot(_.adNetwork)

    rtbAAEs
      .filterNot(_.bidResponseStatus.contains(NoBidReason.QueriesLimitExceeded.prettyValue))
      .foreach { aae =>
        for {
          bidderName <- aae.bidderName
          bidderId   <- aae.bidderId
        } yield {
          val normalizedBidderName = BidderMetrics.normalizedBidderName(bidderName, bidderId)
          val bidderMetrics        = BidderMetrics
          val country              = aae.country.getOrElse("XXX").toUpperCase
          val adType = aae.adType match {
            case AdType.Banner            => BidderMetrics.BANNER
            case AdType.Mrec              => BidderMetrics.BANNER
            case AdType.Interstitial      => BidderMetrics.INTERSTITIAL
            case AdType.Native            => BidderMetrics.NATIVE
            case AdType.Video             => BidderMetrics.VIDEO
            case AdType.NonSkippableVideo => BidderMetrics.VIDEO
          }
          aae.bidStatus match {
            case BidStatus.Win               => bidderMetrics.win(adType, normalizedBidderName)(country).increment()
            case BidStatus.Loss if aae.isBid => bidderMetrics.loss(adType, normalizedBidderName)(country).increment()
            case BidStatus.Loss if aae.bidResponseStatus.isDefined =>
              bidderMetrics.noBid(adType, normalizedBidderName)(country)(aae.bidResponseStatus.get).increment()
          }
        }
      }

    // Send stats to kafka
    val kafkaBidRequests = rtbAAEs.map(_.toKafkaBidRequest)
    kafkaBidRequests.foreach(kafkaProducer.send(bidRequestTopic, _))

    val kafkaBids = rtbAAEs.filter(_.isBid).map(_.toKafkaBid)

    kafkaBids.foreach(kafkaProducer.send(kafkaTopicSettings.bids, _))

  }

  /**
   * Send adNetwork analytics data to bids and bid-requests kafka topics.
   * Bids with `isUnderPrice = true` will be skipped!
   * BidRequests with `isCached = true` will be was!
   *
   * @param entities analyticEntity
   */
  private def sendAdNetworkAnalyticEntityToRtbStats(entities: AuctionAnalyticEntity*) =
    entities
      .filter(_.isBid)
      .filterNot(_.isUnderPrice)
      .map(_.toKafkaBid)
      .foreach(kafkaProducer.send(kafkaTopicSettings.bids, _))

  /**
   * Send adNetwork analytics data to adNetwork bids and adNetwork request kafka topic.
   * Bids with `isUnderPrice = true`flag will be skipped!
   * BidRequests with `isCached = true` flag will be skipped
   *
   * @param entities analyticEntity
   */
  private def sendAdNetworkStats(entities: AuctionAnalyticEntity*) = {
    entities
      .filter(_.isBid)
      .filterNot(_.isUnderPrice)
      .map(_.toKafkaBid)
      .foreach(kafkaProducer.send(kafkaTopicSettings.adNetworkBids, _))
    entities
      .filterNot(_.isCached)
      .map(_.toKafkaBidRequest)
      .foreach(kafkaProducer.send(kafkaTopicSettings.adNetworkRequest, _))
  }
}

object AnalyticsServiceImpl {
  val undefined = "undefined"
}
