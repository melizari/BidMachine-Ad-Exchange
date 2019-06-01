package com.appodealx.exchange.settings.models.seller

final case class Publisher(id: Option[String],
                           name: Option[String],
                           cat: Option[Vector[String]],
                           domain: Option[String])
