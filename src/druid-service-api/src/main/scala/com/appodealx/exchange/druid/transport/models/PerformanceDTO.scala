package com.appodealx.exchange.druid.transport.models

import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.analytics.AdType
import com.appodealx.exchange.settings.models.circe.CirceBuyerSettingsInstances
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, ObjectEncoder}
import io.swagger.annotations.ApiModel
import org.joda.time.Interval

@ApiModel
case class PerformanceDTO(interval: Option[Interval] = None,
                          granularity: Option[String] = Some("day"),
                          country: Option[List[String]] = None,
                          adType: Option[List[AdType]] = None,
                          platform: Option[List[Platform]] = None,
                          direction: Option[String] = None,
                          agency: Option[List[String]] = None,
                          sellerIds: Option[List[Long]] = None,
                          agencyId: Option[Long] = None)

object PerformanceDTO extends CirceBuyerSettingsInstances {
  implicit val performanceDTODecoder: Decoder[PerformanceDTO] = deriveDecoder[PerformanceDTO]
  implicit val performanceDTOEncoder: ObjectEncoder[PerformanceDTO] = deriveEncoder[PerformanceDTO]
}
