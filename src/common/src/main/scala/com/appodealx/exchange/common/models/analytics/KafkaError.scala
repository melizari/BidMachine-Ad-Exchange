package com.appodealx.exchange.common.models.analytics

import com.appodealx.exchange.common.models.auction.{AgencyExternalId, AgencyId}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import io.circe.generic.semiauto.deriveEncoder
import org.joda.time.DateTime


case class KafkaError(timestamp: DateTime,

                      errorCode: Option[String] = None,

                      eventCode: Option[String] = None,
                      actionCode: Option[String] = None,
                      errorReason: Option[String] = None,

                      appId: Option[Long],
                      appIdRaw: Option[String],
                      appName: Option[String],
                      appBundle: Option[String],
                      agencyExternalId: Option[AgencyExternalId],
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
                      @deprecated("10/12/2018", "This type not used in analytics and duplicates adType") creativeType: Option[String] = None,
                      country: Option[String],
                      adNetwork: Option[Boolean] = None,
                      adNetworkName: Option[String] = None,
                      adNetworkPlacementId: Option[String] = None,
                      sellerId: Option[Long] = None,
                      sellerName: Option[String] = None,
                      gdpr: Option[Boolean] = None,
                      dcid: Option[String])

object KafkaError extends CirceModelsInstances {

  implicit val kafkaErrorEncoder = deriveEncoder[KafkaError]

}