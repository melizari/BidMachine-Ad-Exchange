package com.appodealx.exchange.common.models.analytics

import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.auction.{AdProfileId, AgencyId}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import io.circe.generic.semiauto._
import org.joda.time.DateTime


case class KafkaBidRequest(mediationId: Option[String] = None,
                           bidRequestId: String,
                           timestamp: DateTime,
                           adType: AdType,
                           agency: Option[String],
                           agencyId: Option[AgencyId] = None,
                           bidder: Option[String],
                           adProfileId: Option[AdProfileId],
                           adSpaceId: Option[Long],
                           bidPriceLevel: Option[String] = None,
                           bidStatus: BidStatus,
                           bidResponseStatus: Option[String],
                           country: Option[String],
                           platform: Option[Platform],
                           osVersion: Option[String] = None,
                           mediationSdkName: Option[String] = None,
                           mediationSdkVersion: Option[String] = None,
                           dm: Option[String],
                           dmVersion: Option[String],
                           publisherId: Option[String] = None,
                           sellerId: Option[Long],
                           sellerName: Option[String],
                           app: Option[String],
                           appBundle: Option[String] = None,
                           siteDomain: Option[String] = None,
                           ifa: Option[String] = None,
                           adNetwork: Option[Boolean] = None,
                           adNetworkName: Option[String] = None,
                           adNetworkPlacementId: Option[String] = None,
                           dcid: Option[String])

object KafkaBidRequest extends CirceModelsInstances {

  implicit val kafkaBidRequestEncoder = deriveEncoder[KafkaBidRequest]

}