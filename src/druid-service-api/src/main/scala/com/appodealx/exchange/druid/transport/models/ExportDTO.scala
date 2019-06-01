package com.appodealx.exchange.druid.transport.models

import com.appodealx.exchange.common.models.auction.AgencyExternalId
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, ObjectEncoder }
import org.joda.time.DateTime

case class ExportDTO(timestamp: DateTime,
                     country: Option[DictionaryItemDTO] = None,
                     adType: Option[DictionaryItemDTO] = None,
                     platform: Option[DictionaryItemDTO] = None,
                     agency: Option[DictionaryItemDTO] = None,
                     seller: Option[DictionaryItemDTO] = None,
                     agencyName: Option[String] = None,
                     externalAgencyId: Option[AgencyExternalId] = None,
                     app: Option[String] = None,
                     spent: Option[Double] = None,
                     bids: Option[Long] = None,
                     wins: Option[Long] = None,
                     impressions: Option[Long] = None,
                     clicks: Option[Long] = None,
                     finishes: Option[Long] = None,
                     displayRate: Option[Double] = None,
                     ctr: Option[Double] = None,
                     ecpm: Option[Double] = None,
                     sspIncome: Option[Double] = None,
                     exchangeFee: Option[Double] = None,
                     errors: Option[Long] = None,
                     errorsRate: Option[Double] = None,
                     lostImpressions: Option[Long] = None,
                     lostImpressionsRevenue: Option[Double] = None)

object ExportDTO extends CirceBuyerSettingsInstances {
  def empty = ExportDTO(timestamp = DateTime.now())

  implicit val exportResultDecoder: Decoder[ExportDTO]       = deriveDecoder[ExportDTO]
  implicit val exportResultEncoder: ObjectEncoder[ExportDTO] = deriveEncoder[ExportDTO]

}
