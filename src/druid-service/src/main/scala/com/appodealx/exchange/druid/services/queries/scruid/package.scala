package com.appodealx.exchange.druid.services.queries

import ing.wbaa.druid.definitions.FilterOperators._
import ing.wbaa.druid.definitions._

package object scruid {

  def scDimensions(dimensions: List[String]) = dimensions.map(d => DefaultDimension(d, Some(d)))

  def scTenantFilter(id: Long) =
    SelectFilter("agencyExternalId", id.toString) ||
      SelectFilter("bidderAgencyExternalId", id.toString) ||
      SelectFilter("externalAgencyId", id.toString)

  def scSellerFilter(id: Long) = {
    SelectFilter("sellerId", id.toString)
  }

  def scDimensionFilter(dimensions: List[DefaultDimension], values: List[String]) = {
    val filters = values.flatMap(v => dimensions.map(d => SelectFilter(d.dimension, v)))

    filters match {
      case f if f.nonEmpty => Some(OrFilter(f))
      case _               => None
    }
  }
}
