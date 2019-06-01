package com.appodealx.exchange.druid.services.queries.scruid

import cats.syntax.option._
import ing.wbaa.druid.definitions._
import ing.wbaa.druid.definitions.FilterOperators._
import ing.wbaa.druid.{ DruidConfig, GroupByQuery }
import org.joda.time.Interval

/**
 * Rtb error query generator.
 * Filtered by 1005 and 4xx error codes.
 */
object ErrorsQuery extends Query {

  override def make(dataSource: String,
                    interval: Interval,
                    granularity: Granularity,
                    dimensions: List[DefaultDimension],
                    values: List[String],
                    agencyExternalIdTenant: Option[Long],
                    agencyInternalIdTenant: Option[Long]) = {

    val errorCodeFilter = OrFilter(List(SelectFilter("errorCode", "1005"), RegexFilter("errorCode", "^[4][0-9][0-9]$")))

    val scFilters =
      scDimensionFilter(dimensions, values) &&
        errorCodeFilter.some &&
        agencyExternalIdTenant.map(scTenantFilter)

    val scAggregations = LongSumAggregation("errors", "count") :: Nil

    GroupByQuery(
      aggregations = scAggregations,
      intervals = interval.toString :: Nil,
      filter = scFilters,
      dimensions = dimensions,
      granularity = granularity
    )(DruidConfig.DefaultConfig.copy(datasource = dataSource))
  }
}
