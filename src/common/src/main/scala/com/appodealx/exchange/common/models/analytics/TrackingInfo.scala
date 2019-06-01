package com.appodealx.exchange.common.models.analytics

import java.util.UUID

import com.appodealx.exchange.common.models.auction.{AgencyExternalId, AgencyId}
import com.appodealx.exchange.common.models.circe.{CirceModelsInstances, CirceRtbInstances}
import com.appodealx.exchange.common.models.{AppId, PublisherId}
import io.circe.generic.semiauto._
import org.joda.time.DateTime


case class TrackingInfo(bidRequestId: UUID,
                        mediationId: Option[String],
                        timestamp: DateTime,
                        timestampAuction: DateTime,
                        latencyLevel: String,
                        sellerBidFloor: Double,
                        bidFloor: Double,
                        bidPrice: Double,
                        clearPrice: Option[Double], // Price for rtb bidders
                        clearPriceLevel: Option[String],
                        exchangeFee: Double,
                        sspIncome: Double,
                        clearPriceLongX1000: Option[Long],
                        bidFloorLevel: Option[String] = None,
                        bidPriceLevel: Option[String] = None,
                        nurl: Option[String],
                        nurlResponseStatus: Option[String],
                        extSegmentId: Long,
                        extPlacementId: Long,
                        agencyId: Option[AgencyId] = None,
                        extAgencyId: Option[AgencyExternalId] = None,
                        sspAuctionType: Option[Int] = None,

                        // App
                        appBundle: Option[String] = None,
                        appId: Option[AppId] = None,
                        appIdRaw: Option[String] = None,
                        appName: Option[String] = None,
                        appVersion: Option[String] = None,

                        // Site
                        siteId: Option[String] = None,
                        siteName: Option[String] = None,
                        siteDomain: Option[String] = None,
                        siteMobile: Option[Boolean] = None,

                        deviceCountry: Option[String] = None,
                        deviceOs: Option[String] = None,
                        deviceOsVersion: Option[String] = None,
                        deviceIp: Option[String] = None,
                        deviceIpV6: Option[String] = None,
                        deviceConnectionType: Option[String] = None,
                        deviceIfa: Option[String] = None,
                        adType: Option[AdType] = None,
                        adSize: Option[String] = None,
                        publisherId: Option[PublisherId] = None,
                        agencyName: Option[String] = None,
                        bidderName: Option[String] = None,
                        sdkName: Option[String] = None, // mediation sdk name
                        sdkVersion: Option[String] = None, // mediation sdk version
                        displayManager: Option[String] = None,
                        displayManagerVersion: Option[String] = None,
                        w: Option[Long] = None,
                        h: Option[Long] = None,
                        adomain: Option[List[String]] = None,
                        cid: Option[String] = None,
                        crid: Option[String] = None,
                        isNewSdkVersion: Option[Boolean] = None,
                        externalCampaignImageId: Option[Long] = None,
                        sellerId: Option[Long] = None,
                        sellerName: Option[String] = None,
                        adNetwork: Option[Boolean] = None,
                        adNetworkName: Option[String] = None,
                        adNetworkPlacementId: Option[String] = None,
                        gdpr: Option[Boolean] = None,
                        adSpaceId: Option[Long] = None,

                        sessionNumber: Option[Long] = None,
                        impressionNumber: Option[Long] = None,
                        dcid: Option[String],
                        trackingMethod: String,
                        latency: Option[Double] = None)

object TrackingInfo extends CirceModelsInstances with CirceRtbInstances {

  implicit val trackingInfoDecoder = deriveDecoder[TrackingInfo]
  implicit val trackingInfoEncoder = deriveEncoder[TrackingInfo]

}