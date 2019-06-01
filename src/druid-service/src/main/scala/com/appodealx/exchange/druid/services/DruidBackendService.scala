package com.appodealx.exchange.druid.services

import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.analytics.AdType
import com.appodealx.exchange.druid.transport.models.{DictionaryItemDTO, ExportDTO, ReportSSPResponseRow}
import ing.wbaa.druid.definitions.Granularity
import org.joda.time.Interval

import scala.concurrent.{ExecutionContext, Future}


trait DruidBackendService {

  def query(interval: Interval,
            granularity: Granularity,
            country: Option[List[String]] = None,
            adType: Option[List[AdType]] = None,
            platform: Option[List[Platform]] = None,
            agency: Option[List[String]] = None,
            sellerIds: Option[List[Long]] = None,
            direction: Option[String] = Some("ascending"),
            isExport: Boolean = false,
            agencyInternalId: Option[Long] = None,
            agencyExternalId: Option[Long] = None
           )(implicit ex: ExecutionContext): Future[List[ExportDTO]]

  def agencies(interval: Interval)(implicit ex: ExecutionContext): Future[List[DictionaryItemDTO]]

  def countries(interval: Interval)(implicit ex: ExecutionContext): Future[List[DictionaryItemDTO]]

  def sellers(interval: Interval)(implicit ex: ExecutionContext): Future[List[DictionaryItemDTO]]

  def sellerReport(id: Long, interval: Interval)(implicit ex: ExecutionContext): Future[Iterable[ReportSSPResponseRow]]

}