package com.appodealx.exchange.settings.models.seller

final case class AppSettings(blockedBidders: Option[Vector[String]],
                             blockedCategories: Option[Vector[String]],
                             blockedAdvertisers: Option[Vector[String]])
