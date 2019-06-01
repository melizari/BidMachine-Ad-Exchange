package com.appodealx.exchange.druid.models.dto

import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.analytics.AdType
import com.appodealx.exchange.common.models.auction.AgencyExternalId
import com.appodealx.exchange.druid.models.dto.Event.HyperLoglog
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, ObjectEncoder }

case class Event(country: Option[String] = None,
                 adType: Option[AdType] = None,
                 agency: Option[String] = None,
                 externalAgencyId: Option[AgencyExternalId] = None,
                 agencyExternalId: Option[AgencyExternalId] = None, //fix for bid-requests
                 bidderAgencyExternalId: Option[AgencyExternalId] = None,
                 agencyName: Option[String] = None,
                 deviceOs: Option[Platform] = None,
                 platform: Option[Platform] = None,
                 sellerId: Option[Long] = None,
                 sellerName: Option[String] = None,
                 bids: Option[Long] = None,
                 wins: Option[Long] = None,
                 impressions: Option[Long] = None,
                 clearPrice: Option[Double] = None,
                 clearPriceLongX1000: Option[Long] = None,
                 @deprecated("Will be removed after completely stop sending predicted price. Left for backward compatibility.", "01.03.2019") predictedPrice: Option[Double] = None,
                 clicks: Option[Long] = None,
                 finishes: Option[Long] = None,
                 sspIncome: Option[Double] = None,
                 exchangeFee: Option[Double] = None,
                 errors: Option[Long] = None,
                 lostImpressions: Option[HyperLoglog] = None,
                 lostImpressionsClearingPriceSum: Option[Double] = None,
                 @deprecated("Will be removed after completely stop sending predicted price. Left for backward compatibility.", "01.03.2019") lostImpressionsPredictedPriceSum: Option[Double] = None
                )

object Event extends CirceBuyerSettingsInstances {

  type HyperLoglog = Double

  implicit val eventDecoder: Decoder[Event]       = deriveDecoder[Event]
  implicit val eventEncoder: ObjectEncoder[Event] = deriveEncoder[Event]
}
