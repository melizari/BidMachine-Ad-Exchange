package com.appodealx.exchange.settings.persistance.buyer.tables

import com.appodealx.exchange.common.models.Platform


trait EnumMappingInstances {

  implicit val platformEnumMapping = enumMapping(Platform)
  implicit val platformListEnumMapping = enumListMapping(Platform)

}
