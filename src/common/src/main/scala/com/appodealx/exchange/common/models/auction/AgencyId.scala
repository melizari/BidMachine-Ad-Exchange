package com.appodealx.exchange.common.models.auction

import com.appodealx.exchange.common.models.Id
import slick.lifted.MappedTo

case class AgencyId(value: Id) extends AnyVal with MappedTo[Id]
