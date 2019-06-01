package com.appodealx.exchange.druid.models.scruid.models

import java.time.ZonedDateTime

import io.circe.generic.semiauto.deriveDecoder
import io.circe.java8.time._

/**
  *
  * @param date           Date in "yyyy-MM-ddTHH:mm:ss.SSSZ" format
  * @param country        Country in "ISO 3166-1 alpha-2" format.
  *                       May ne NULL! TODO: Need to fix auction gateway for providing default ZZ country.
  * @param `publisher_id` Seller Id
  * @param `app_name`     App name, may be NULL
  * @param `app_bundle`   App bundle, may be NULL
  * @param platform       Device OS, may be NULL
  * @param impressions    Impressions count
  * @param clicks         Clicks count
  * @param ctr            Click through rate
  * @param ecpm           ECPM
  * @param sspIncome      Seller income, measured in ECPM (USD for 1000 impressions).
  */
case class ReportResult(date: Option[ZonedDateTime] = None,
                        country: Option[String] = Some("ZZ"),
                        `publisher_id`: String,
                        `app_name`: Option[String],
                        `app_bundle`: Option[String],
                        platform: Option[String] = Some("null"),
                        `ad_type`: Option[String] = None,
                        impressions: Option[Long] = None,
                        clicks: Option[Long] = None,
                        ctr: Option[Double] = None,
                        ecpm: Option[Double] = None,
                        sspIncome: Option[Double] = None)

object ReportResult extends JavaTimeDecoders {
  implicit val reportResultDecode = deriveDecoder[ReportResult]
}

