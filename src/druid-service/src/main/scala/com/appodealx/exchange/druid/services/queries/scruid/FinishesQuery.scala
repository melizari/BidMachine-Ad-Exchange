package com.appodealx.exchange.druid.services.queries.scruid

import ing.wbaa.druid.definitions.{ DefaultDimension, Granularity, LongSumAggregation }
import ing.wbaa.druid.definitions.FilterOperators._
import ing.wbaa.druid.{ DruidConfig, GroupByQuery }
import org.joda.time.Interval

object FinishesQuery extends Query {
  override def make(dataSource: String,
                    interval: Interval,
                    granularity: Granularity,
                    dimensions: List[DefaultDimension],
                    values: List[String],
                    agencyExternalIdTenant: Option[Long],
                    agencyInternalIdTenant: Option[Long]) = {

    val scAggregations = LongSumAggregation("finishes", "count") :: Nil

    val scFilters = scDimensionFilter(dimensions, values) && agencyExternalIdTenant.map(scTenantFilter)

    GroupByQuery(
      aggregations = scAggregations,
      intervals = interval.toString :: Nil,
      filter = scFilters,
      dimensions = dimensions,
      granularity = granularity
    )(DruidConfig.DefaultConfig.copy(datasource = dataSource))
  }
}
