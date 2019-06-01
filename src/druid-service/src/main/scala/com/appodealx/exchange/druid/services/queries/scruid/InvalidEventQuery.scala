package com.appodealx.exchange.druid.services.queries.scruid

import cats.syntax.option._
import com.appodealx.exchange.common.models.analytics.InvalidEventStatus
import ing.wbaa.druid.definitions.FilterOperators._
import ing.wbaa.druid.definitions._
import ing.wbaa.druid.{ DruidConfig, DruidQuery, GroupByQuery }
import org.joda.time.Interval

object InvalidEventQuery extends Query {

  def make(dataSource: String,
           interval: Interval,
           granularity: Granularity,
           dimensions: List[DefaultDimension],
           values: List[String],
           agencyExternalIdTenant: Option[Long] = None,
           agencyInternalIdTenant: Option[Long] = None): DruidQuery = {

    val scAggregations = List(
      HyperUniqueAggregation("lostImpressions", "auctionCount"),
      DoubleSumAggregation("lostImpressionsPredictedPriceSum", "predictedPriceSum"),
      DoubleSumAggregation("lostImpressionsClearingPriceSum", "clearPriceSum")
    )

    val impressionOnlyFilter = SelectFilter("event", "impression")

    val notDuplicatedImpressionFilter = !SelectFilter("eventStatus", InvalidEventStatus.DUPLICATE)

    val scFilters =
      impressionOnlyFilter.some &&
        scDimensionFilter(dimensions, values) &&
        notDuplicatedImpressionFilter.some &&
        agencyExternalIdTenant.map(scTenantFilter)

    GroupByQuery(
      aggregations = scAggregations,
      intervals = interval.toString :: Nil,
      filter = scFilters,
      dimensions = dimensions,
      granularity = granularity
    )(DruidConfig.DefaultConfig.copy(datasource = dataSource))
  }
}
