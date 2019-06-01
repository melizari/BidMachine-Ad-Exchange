package com.appodealx.exchange.settings.models.seller

final case class App(id: Option[Long],
                     eid: Option[String],
                     ks: Option[String],
                     sellerId: Option[Long],
                     platform: Platform,
                     name: Option[String],
                     bundle: Option[String],
                     domain: Option[String],
                     storeurl: Option[String],
                     storeid: Option[String],
                     cat: Option[Vector[String]],
                     privacypolicy: Option[Boolean],
                     paid: Option[Boolean],
                     publisher: Option[Publisher],
                     keywords: Option[String],
                     settings: Option[AppSettings])
