package com.appodealx.exchange.common.models.analytics

import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.auction._
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.common.models.rtb._
import com.appodealx.openrtb._
import io.circe.generic.semiauto.deriveEncoder
import org.joda.time.DateTime


case class KafkaBid(timestamp: DateTime,
                    bidRequestId: String,
                    adType: AdType,
                    adSize: String,
                    adSpaceId: Option[Long] = None,
                    sspAuctionType: Option[Int] = None,

                    // App
                    app: Option[String] = None, // `AppExternalId`|`AppName`
                    appExternalId: Option[String] = None,
                    appPublisherExternalId: Option[String] = None,
                    appName: Option[String] = None,
                    appPublisherName: Option[String] = None,
                    appBundle: String,
                    appCategories: Option[List[String]] = None,
                    appVersion: Option[String] = None,

                    // Site
                    siteId: Option[String] = None,
                    sitePublisherId: Option[String] = None,
                    siteName: Option[String] = None,
                    sitePublisherName: Option[String] = None,
                    siteDomain: Option[String] = None,
                    siteCategories: Option[List[String]] = None,
                    siteSectionCategories: Option[List[String]] = None,
                    sitePageCategories: Option[List[String]] = None,
                    sitePage: Option[String] = None,
                    siteRef: Option[String] = None,
                    siteSearch: Option[String] = None,
                    siteMobile: Option[Boolean] = None,
                    sitePrivacypolicy: Option[Boolean] = None,
                    siteKeywords: Option[String] = None,

                    sellerId: Option[Long],
                    sellerName: Option[String],

                    agencyId: Option[AgencyId],
                    agencyExternalId: Option[AgencyExternalId] = None,
                    agencyName: Option[String],
                    bidderId: Option[BidderId],
                    bidderName: Option[String],
                    bidderAdProfileId: Option[AdProfileId],
                    bidderRtbVersion: Option[Version],
                    bidFloorLevel: String,
                    bidStatus: BidStatus,
                    bidAdomain: Option[List[String]] = None,
                    bidBundle: Option[String] = None,
                    bidCategories: Option[List[String]] = None,
                    bidAttributes: Option[List[String]] = None,
                    bidImpressionUrl: Option[String] = None,
                    bidCampaignId: Option[String] = None,
                    bidCreativeId: Option[String] = None,
                    bidCreativeRating: Option[QagMediaRating] = None,
                    bidNurlDomain: Option[String] = None,
                    bidNurlStatus: Option[String] = None,
                    bidHasAdm: Boolean,
                    bidPriceLevel: String,
                    clearingPriceLevel: Option[String] = None,
                    clearingPrice: Option[Double] = None,
                    requestBlockedCategories: Option[List[String]] = None,
                    requestBlockedAdvertisers: Option[List[String]] = None,
                    requestBlockedAttributes: Option[List[String]] = None,
                    deviceOs: Option[Platform] = None,
                    deviceOsVersion: Option[String] = None,
                    deviceCarrier: Option[String] = None,
                    deviceMake: Option[String] = None,
                    deviceModel: Option[String] = None,
                    deviceType: Option[DeviceType] = None,
                    deviceIfa: Option[String] = None,
                    deviceIp: Option[String] = None,
                    deviceIpV6: Option[String] = None,
                    deviceConnectionType: Option[String] = None,
                    mediationSdkName: Option[String] = None,
                    mediationSdkVersion: Option[String] = None,
                    displayManager: Option[String] = None,
                    displayManagerVersion: Option[String] = None,
                    country: Option[String] = None,
                    coppa: Boolean,
                    test: Boolean,
                    adNetwork: Option[Boolean] = None,
                    adNetworkName: Option[String] = None,
                    adNetworkPlacementId: Option[String] = None,
                    cached: Option[Boolean] = None,
                    isUnderPrice: Option[Boolean] = None,
                    sessionNumber: Option[Long] = None,
                    impressionNumber: Option[Long] = None,
                    ipLocationService: Option[String] = None,
                    dcid: Option[String])

object KafkaBid extends CirceModelsInstances {

  implicit val circeKafkaBidEncoder = deriveEncoder[KafkaBid]

}