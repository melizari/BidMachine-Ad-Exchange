package com.appodealx.exchange.common.models.analytics

import com.appodealx.exchange.common.models.auction.{AgencyExternalId, AgencyId}
import org.joda.time.DateTime


case class ErrorContext(timestamp: DateTime,
                        appId: Option[Long],
                        appIdRaw: Option[String],
                        appName: Option[String],
                        appBundle: Option[String],
                        extAgencyId: Option[AgencyExternalId],
                        agencyId: Option[AgencyId],
                        agencyName: Option[String],
                        deviceOs: Option[String],
                        deviceOsVersion: Option[String] = None,
                        deviceIfa: Option[String] = None,
                        displayManager: Option[String], // For rtb auction is `appodeal`, for PB is adNetworkName.toLoweCase
                        displayManagerVersion: Option[String],
                        sdkName: Option[String] = None, // mediation sdk name ("appodeal")
                        sdkVersion: Option[String] = None, // mediation sdk version
                        cid: Option[String] = None,
                        crid: Option[String] = None,
                        adType: Option[AdType] = None,
                        country: Option[String] = None,
                        adNetwork: Option[Boolean] = None,
                        adNetworkName: Option[String] = None,
                        adNetworkPlacementId: Option[String] = None,
                        sellerId: Option[Long] = None,
                        sellerName: Option[String] = None,
                        gdpr: Option[Boolean] = None)