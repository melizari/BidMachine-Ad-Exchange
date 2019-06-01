package com.appodealx.exchange.common.models

import slick.lifted.MappedTo

case class AppId(value: Long) extends AnyVal with MappedTo[Long]
