package com.appodealx.exchange.common.models.analytics

import com.appodealx.exchange.common.models.auction.{AgencyExternalId, AgencyId}
import com.appodealx.exchange.common.models.{AppId, PublisherId}
import com.appodealx.openrtb.ConnectionType
import org.joda.time.DateTime


case class CallbackContext(bidRequestId: String,
                           impId: Option[String],
                           originalBidFloor: Double,
                           bidFloor: Double,
                           bidPrice: Double,
                           clearingPrice: Double,
                           exchangeFee: Double,
                           sspIncome: Double,
                           timestamp: DateTime = DateTime.now,

                           // App
                           appBundle: Option[String] = None,
                           appId: Option[AppId] = None,
                           appIdRaw: Option[String] = None,
                           appName: Option[String] = None,
                           appVersion: Option[String] = None,

                           sspAuctionType: Option[Int] = None,
                           country: Option[String] = None,
                           deviceOs: Option[String] = None,
                           deviceOsVersion: Option[String] = None,
                           deviceIp: Option[String] = None,
                           deviceIpV6: Option[String] = None,
                           deviceConnectionType: Option[ConnectionType] = None,
                           adType: Option[AdType] = None,
                           adSize: Option[String] = None,
                           agencyId: Option[AgencyId] = None,
                           externalAgencyId: Option[AgencyExternalId] = None,
                           externalPublisherId: Option[PublisherId] = None,
                           ifa: Option[String] = None,
                           agencyName: Option[String] = None,
                           bidderName: Option[String] = None,
                           sdkName: Option[String] = None, // mediation sdk name ("appodeal")
                           sdkVersion: Option[String] = None, // mediation sdk version
                           displayManager: Option[String] = None, // render sdk name
                           displayManagerVersion: Option[String] = None, // render sdk version
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

                           sesN: Option[Long] = None,
                           impN: Option[Long] = None)
