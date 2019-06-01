package services

import java.util.UUID

import com.appodealx.exchange.common.models.CallbackTrackingMethod
import com.appodealx.exchange.common.models.analytics.TrackingEventType._
import com.appodealx.exchange.common.models.analytics.{CallbackContext, KafkaError, NonValidTrackingInfo, TrackingEventType, TrackingInfo}
import com.appodealx.exchange.common.models.rtb._
import com.appodealx.exchange.common.services.kafka.CirceKafkaProducer
import com.appodealx.exchange.common.utils.analytics.priceLevel
import monix.eval.Task
import org.joda.time.DateTime
import play.api.mvc.RequestHeader
import play.api.{Configuration, Logger}
import utils.TrackerUtils._

import scala.concurrent.ExecutionContext


class EmitterService(kafkaProducer: CirceKafkaProducer,
                     configuration: Configuration,
                     dcSettings: DatacenterMetadataSettings,
                     kafkaTopicSettings: KafkaTopicSettings) {

  def sendKafkaError(error: KafkaError)(implicit executionContext: ExecutionContext) =
    Task.deferFuture(kafkaProducer.send(kafkaTopicSettings.errors, error))

  /**
    * Send non valid events to kafka
    *
    * @param timestamp           event timestamp
    * @param event               event status
    * @param context             optional callback context
    * @param externalSegmentId   optional segment id
    * @param externalPlacementId optional placement id
    */
  def sendNonValidEvents(timestamp: DateTime,
                         event: TrackingEventType,
                         eventStatus: String,
                         trackingMethod: CallbackTrackingMethod,
                         context: Option[CallbackContext] = None,
                         externalSegmentId: Option[Long] = None,
                         externalPlacementId: Option[Long] = None,
                        )(implicit executionContext: ExecutionContext) = {

    val clearingPriceOpt = context.map(_.clearingPrice)

    val info = NonValidTrackingInfo(
      timestamp = timestamp,
      eventId = UUID.randomUUID(),
      event = event.prettyValue,
      eventStatus = eventStatus,

      bidRequestId = context.map(_.bidRequestId).map(UUID.fromString),
      latencyLevel = context.map(_.timestamp).map(latencyLevel(_, timestamp)),
      clearPrice = clearingPriceOpt,
      clearPriceLevel = clearingPriceOpt.map(priceLevel),
      bidFloor = context.map(_.bidFloor),
      sellerBidFloor = context.map(_.originalBidFloor),
      bidPrice = context.map(_.bidPrice),
      exchangeFee = context.map(_.exchangeFee),
      sspIncome = context.map(_.sspIncome),
      extSegmentId = externalSegmentId,
      extPlacementId = externalPlacementId,
      agencyId = context.flatMap(_.agencyId),
      externalAgencyId = context.flatMap(_.externalAgencyId),

      // App
      appBundle = context.flatMap(_.appBundle),
      appId = context.flatMap(_.appId),
      appIdRaw = context.flatMap(_.appIdRaw),
      appName = context.flatMap(_.appName),
      appVersion = context.flatMap(_.appVersion),

      country = context.flatMap(_.country),
      deviceOs = context.flatMap(_.deviceOs),
      deviceOsVersion = context.flatMap(_.deviceOsVersion),
      deviceIp = context.flatMap(_.deviceIp),
      deviceIpV6 = context.flatMap(_.deviceIpV6),
      deviceConnectionType = context.flatMap(_.deviceConnectionType.map(_.prettyValue)),
      adType = context.flatMap(_.adType),
      adSize = context.flatMap(_.adSize),
      publisherId = context.flatMap(_.externalPublisherId),
      deviceIfa = context.flatMap(_.ifa),
      agencyName = context.flatMap(_.agencyName),
      bidderName = context.flatMap(_.bidderName),
      sdkName = context.flatMap(_.sdkName),
      sdkVersion = context.flatMap(_.sdkVersion),
      displayManager = context.flatMap(_.displayManager),
      displayManagerVersion = context.flatMap(_.displayManagerVersion),
      bidFloorLevel = context.map(c => priceLevel(c.bidFloor)),
      bidPriceLevel = context.map(c => priceLevel(c.bidPrice)),
      adomain = context.flatMap(_.adomain),
      cid = context.flatMap(_.cid),
      crid = context.flatMap(_.crid),
      isNewSdkVersion = context.flatMap(_.isNewSdkVersion),
      externalCampaignImageId = context.flatMap(_.externalCampaignImageId),
      sellerId = context.flatMap(_.sellerId),
      sellerName = context.flatMap(_.sellerName),
      adNetwork = Some(context.flatMap(_.adNetwork).getOrElse(false)),
      adNetworkName = context.flatMap(_.adNetworkName),
      adNetworkPlacementId = context.flatMap(_.adNetworkPlacementId),
      gdpr = context.flatMap(_.gdpr),
      adSpaceId = context.flatMap(_.adSpaceId),

      sessionNumber = context.flatMap(_.sesN),
      impressionNumber = context.flatMap(_.impN),
      dcid = Some(dcSettings.dcid),
      trackingMethod = trackingMethod.prettyValue
    )

    Task.deferFuture(kafkaProducer.send(kafkaTopicSettings.invalidEvents, info))

  }

  /**
    * Send tracking event to kafka
    *
    * @param timestamp           tracking complete timestamp
    * @param timestampAuction    auction complete timestamp
    * @param clearPrice          clearing price
    * @param bidRequestId        request bid id
    * @param event               string representation of tracking (click or impression)
    * @param context             exchange id with custom parameters for stat
    * @param externalSegmentId   ext segment id
    * @param externalPlacementId ext placement id
    * @return Future of Unit
    */
  def send(timestamp: DateTime,
           timestampAuction: DateTime,
           clearPrice: Double,
           bidRequestId: String,
           event: TrackingEventType,
           nurlDomain: Option[String],
           nurlResponseStatus: Option[String],
           context: Option[CallbackContext],
           externalSegmentId: Long,
           externalPlacementId: Long,
           trackingMethod: CallbackTrackingMethod,
           latency: Option[Double] = None)(implicit requestHeader: RequestHeader, executionContext: ExecutionContext) = {

    import com.appodealx.exchange.common.utils.PriceAsLong

    val clearingPriceOpt = Some(clearPrice)

    val trackingInfo =
      TrackingInfo(
        bidRequestId = UUID.fromString(bidRequestId),
        mediationId = context.flatMap(_.impId),
        timestamp = timestamp,
        timestampAuction = timestampAuction,
        latencyLevel = latencyLevel(timestampAuction, timestamp),
        clearPrice = clearingPriceOpt,
        clearPriceLevel = clearingPriceOpt.map(priceLevel),
        clearPriceLongX1000 = clearingPriceOpt.map(_.asLongX1K),
        nurl = nurlDomain,
        nurlResponseStatus = nurlResponseStatus,
        bidFloor = context.map(_.bidFloor).getOrElse(0.0),
        sellerBidFloor = context.map(_.originalBidFloor).getOrElse(0.0),
        bidPrice = context.map(_.bidPrice).getOrElse(0.0),
        exchangeFee = context.map(_.exchangeFee).getOrElse(0.0),
        sspIncome = context.map(_.sspIncome).getOrElse(0.0),
        extSegmentId = externalSegmentId,
        extPlacementId = externalPlacementId,
        agencyId = context.flatMap(_.agencyId),
        extAgencyId = context.flatMap(_.externalAgencyId),
        sspAuctionType = context.flatMap(_.sspAuctionType),

        // App
        appBundle = context.flatMap(_.appBundle),
        appId = context.flatMap(_.appId),
        appIdRaw = context.flatMap(_.appIdRaw),
        appName = context.flatMap(_.appName),
        appVersion = context.flatMap(_.appVersion),


        deviceCountry = context.flatMap(_.country),
        deviceOs = context.flatMap(_.deviceOs),
        deviceOsVersion = context.flatMap(_.deviceOsVersion),
        deviceIp = context.flatMap(_.deviceIp),
        deviceIpV6 = context.flatMap(_.deviceIpV6),
        deviceConnectionType = context.flatMap(_.deviceConnectionType.map(_.prettyValue)),
        adType = context.flatMap(_.adType),
        adSize = context.flatMap(_.adSize),
        publisherId = context.flatMap(_.externalPublisherId),
        deviceIfa = context.flatMap(_.ifa),
        agencyName = context.flatMap(_.agencyName),
        bidderName = context.flatMap(_.bidderName),
        sdkName = context.flatMap(_.sdkName),
        sdkVersion = context.flatMap(_.sdkVersion),
        displayManager = context.flatMap(_.displayManager),
        displayManagerVersion = context.flatMap(_.displayManagerVersion),
        bidFloorLevel = context.map(c => priceLevel(c.bidFloor)),
        bidPriceLevel = context.map(c => priceLevel(c.bidPrice)),
        adomain = context.flatMap(_.adomain),
        cid = context.flatMap(_.cid),
        crid = context.flatMap(_.crid),
        isNewSdkVersion = context.flatMap(_.isNewSdkVersion),
        externalCampaignImageId = context.flatMap(_.externalCampaignImageId),
        sellerId = context.flatMap(_.sellerId),
        sellerName = context.flatMap(_.sellerName),
        adNetwork = Some(context.flatMap(_.adNetwork).getOrElse(false)),
        adNetworkName = context.flatMap(_.adNetworkName),
        adNetworkPlacementId = context.flatMap(_.adNetworkPlacementId),
        gdpr = context.flatMap(_.gdpr),
        adSpaceId = context.flatMap(_.adSpaceId),

        sessionNumber = context.flatMap(_.sesN),
        impressionNumber = context.flatMap(_.impN),
        dcid = Some(dcSettings.dcid),
        trackingMethod = trackingMethod.prettyValue,
        latency = latency
      )

    if (event == ImpressionEvent) {
      Task.deferFuture(kafkaProducer.send(kafkaTopicSettings.impressions, trackingInfo))
    } else if (event == ClickEvent) {
      Task.deferFuture(kafkaProducer.send(kafkaTopicSettings.clicks, trackingInfo))
    } else if (event == FinishEvent) {
      Task.deferFuture(kafkaProducer.send(kafkaTopicSettings.finish, trackingInfo))
    } else if (event == FillEvent) {
      Task.deferFuture(kafkaProducer.send(kafkaTopicSettings.fills, trackingInfo))
    } else if (event == CustomEvent) {
      Task.deferFuture(kafkaProducer.send(kafkaTopicSettings.`custom-loaded-event`, trackingInfo))
    } else {
      Logger.warn(s"Nothing send to kafka with event: $event")
      Task.now(())
    }
  }
}
