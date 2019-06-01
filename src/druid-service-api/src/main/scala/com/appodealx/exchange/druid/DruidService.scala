package com.appodealx.exchange.druid

import akka.{Done, NotUsed}
import com.appodealx.exchange.common.models.circe.CirceModelsInstances
import com.appodealx.exchange.druid.transport.models.{ExportDTO, PerformanceDTO, _}
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{CircuitBreaker, Descriptor, Service, ServiceCall}
import io.circe.Printer

abstract class DruidService extends Service with CirceModelsInstances {

  def healthCheck: ServiceCall[NotUsed, Done]

  def sspReport(id: Long, start: String, end: String, format: Option[String], csvWithHeader: Option[Boolean]): ServiceCall[NotUsed, String]

  def performance: ServiceCall[PerformanceDTO, List[ExportDTO]]

  def adTypes: ServiceCall[NotUsed, List[DictionaryItemDTO]]

  def platforms: ServiceCall[NotUsed, List[DictionaryItemDTO]]

  def countries(date: Option[String], start: Option[String], end: Option[String]): ServiceCall[NotUsed, List[DictionaryItemDTO]]

  def agencies(date: Option[String], start: Option[String], end: Option[String]): ServiceCall[NotUsed, List[DictionaryItemDTO]]

  def sellers(date: Option[String], start: Option[String], end: Option[String]): ServiceCall[NotUsed, List[DictionaryItemDTO]]

  override def descriptor: Descriptor = {

    import Service._
    import com.appodealx.exchange.common.utils.circe.CirceLagomMessageSerializer._
    implicit val compactPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

    named("druid-service")
      .withCalls(
        restCall(Method.GET, "/api/druid/healthcheck", healthCheck),
        restCall(Method.POST, "/api/druid/performance", performance),
        restCall(Method.GET, "/api/druid/performance/adType", adTypes),
        restCall(Method.GET, "/api/druid/performance/platform", platforms),
        restCall(Method.GET, "/api/druid/performance/country?date&start&end", countries _),
        restCall(Method.GET, "/api/druid/performance/agency?date&start&end", agencies _),
        restCall(Method.GET, "/api/druid/performance/seller?date&start&end", sellers _),
        pathCall("/api/druid/report/seller?id&start&end&format&csv_with_header", sspReport _)(MessageSerializer.NotUsedMessageSerializer, MessageSerializer.StringMessageSerializer)
          .withCircuitBreaker(CircuitBreaker.identifiedBy("ssp-report"))
      )
      .withAutoAcl(true)
  }

}
