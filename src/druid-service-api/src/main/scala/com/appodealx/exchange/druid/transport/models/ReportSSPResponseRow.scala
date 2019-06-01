package com.appodealx.exchange.druid.transport.models

import io.circe.{Decoder, ObjectEncoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.swagger.annotations.ApiModel


@ApiModel
/**
  * Report row model for ssp. For APDX-871.
  *
  * @param date           Representing a day in the date range. In %Y-%m-%d format.
  * @param country        2 letter country code ( ISO Alpha-2), default ZZ.
  * @param `publisher_id` AppodealX Publisher ID (seller id).
  * @param `app_name`     Name of App.
  * @param `app_bundle`   Numeric ID or text app bundle.
  * @param `platform`     Platform like “ios” or “android”.
  * @param `ad_type`      Ad type
  * @param impressions    Number of impressions, default 0.
  * @param clicks         Number of clicks, default 0.
  * @param ctr            Click through rate – calculated by click/impressions * 100.
  * @param ecpm           Effective CPM Calculated by Revenue/Impressions * 1000.
  * @param revenue        Amount of revenue, USD, default 0.00.
  */
case class ReportSSPResponseRow(date: String,
                                country: String,
                                `publisher_id`: Long,
                                `app_name`: Option[String] = None,
                                `app_bundle`: Option[String] = None,
                                `platform`: Option[String] = None,
                                `ad_type`: Option[String] = None,
                                impressions: Long,
                                clicks: Long,
                                ctr: Double,
                                ecpm: Double,
                                revenue: Double)

object ReportSSPResponseRow {
  implicit val reportSSPResponseRowDecoder: Decoder[ReportSSPResponseRow] = deriveDecoder[ReportSSPResponseRow]
  implicit val reportSSPResponseRowEncoder: ObjectEncoder[ReportSSPResponseRow] = deriveEncoder[ReportSSPResponseRow]
}
