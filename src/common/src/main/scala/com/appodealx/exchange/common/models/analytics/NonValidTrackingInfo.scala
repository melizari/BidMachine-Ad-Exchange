package com.appodealx.exchange.common.models.analytics

import java.util.UUID

import com.appodealx.exchange.common.models.auction.{AgencyExternalId, AgencyId}
import com.appodealx.exchange.common.models.circe.{CirceModelsInstances, CirceRtbInstances}
import com.appodealx.exchange.common.models.{AppId, PublisherId}
import io.circe.Json
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.joda.time.DateTime

case class NonValidTrackingInfo(timestamp: DateTime,
                                eventId: UUID,
                                event: String,
                                eventStatus: String,

                                bidRequestId: Option[UUID] = None,
                                timestampAuction: Option[DateTime] = None,
                                latencyLevel: Option[String] = None,
                                sellerBidFloor: Option[Double] = None,
                                bidFloor: Option[Double] = None,
                                bidPrice: Option[Double] = None,
                                clearPrice: Option[Double] = None,
                                clearPriceLevel: Option[String] = None,
                                exchangeFee: Option[Double] = None,
                                sspIncome: Option[Double] = None,
                                bidFloorLevel: Option[String] = None,
                                bidPriceLevel: Option[String] = None,
                                extSegmentId: Option[Long] = None,
                                extPlacementId: Option[Long] = None,
                                agencyId: Option[AgencyId] = None,
                                externalAgencyId: Option[AgencyExternalId] = None,

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

                                country: Option[String] = None,
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
                                trackingMethod: String)

object NonValidTrackingInfo extends CirceModelsInstances with CirceRtbInstances {
  implicit val nonValidTrackingInfoDecoder = deriveDecoder[NonValidTrackingInfo]
  implicit val nonValidTrackingInfoEncoder = deriveEncoder[NonValidTrackingInfo]
}
