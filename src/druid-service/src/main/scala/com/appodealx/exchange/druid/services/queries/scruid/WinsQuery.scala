package com.appodealx.exchange.druid.services.queries.scruid

import cats.syntax.option._
import ing.wbaa.druid.definitions._
import ing.wbaa.druid.definitions.FilterOperators._
import ing.wbaa.druid.{ DruidConfig, GroupByQuery }
import org.joda.time.Interval

object WinsQuery extends Query {
  override def make(dataSource: String,
                    interval: Interval,
                    granularity: Granularity,
                    dimensions: List[DefaultDimension],
                    values: List[String],
                    agencyExternalIdTenant: Option[Long],
                    agencyInternalIdTenant: Option[Long]) = {

    val scAggregations = LongSumAggregation("wins", "bidRequestCount") :: Nil

    val winFilter = SelectFilter("bidStatus", "win")

    val scFilters = winFilter.some && agencyExternalIdTenant.map(scTenantFilter) && scDimensionFilter(dimensions, values)

    GroupByQuery(
      aggregations = scAggregations,
      intervals = interval.toString :: Nil,
      filter = scFilters,
      dimensions = dimensions,
      granularity = granularity
    )(DruidConfig.DefaultConfig.copy(datasource = dataSource))
  }
}
