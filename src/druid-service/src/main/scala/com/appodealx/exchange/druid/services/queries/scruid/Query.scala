package com.appodealx.exchange.druid.services.queries.scruid

import ing.wbaa.druid.DruidQuery
import ing.wbaa.druid.definitions.{ DefaultDimension, Granularity }
import org.joda.time.Interval

trait Query {
  def make(dataSource: String,
           interval: Interval,
           granularity: Granularity,
           dimensions: List[DefaultDimension],
           values: List[String],
           agencyExternalIdTenant: Option[Long] = None,
           agencyInternalIdTenant: Option[Long] = None): DruidQuery
}
