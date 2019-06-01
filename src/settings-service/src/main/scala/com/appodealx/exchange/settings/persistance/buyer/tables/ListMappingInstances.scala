package com.appodealx.exchange.settings.persistance.buyer.tables

import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.common.models.Country


trait ListMappingInstances {

  implicit val countriesListMapping = {
    implicit val dummyWitness = PostgresProfile.ElemWitness.AnyWitness.asInstanceOf[PostgresProfile.ElemWitness[Country]]
    new PostgresProfile.SimpleArrayJdbcType[Country]("text").to(_.toList)
  }

}
