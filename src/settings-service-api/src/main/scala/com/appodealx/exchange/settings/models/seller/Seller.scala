package com.appodealx.exchange.settings.models.seller

import play.api.libs.json.Json

case class Seller(id: Option[Long],
                  ks: Option[String],
                  name: Option[String],
                  fee: Option[Double],
                  active: Option[Boolean] = Some(false),
                  bcat: Option[List[String]] = None,
                  badv: Option[List[String]] = None,
                  bapp: Option[List[String]] = None)

object Seller {

  implicit val sellerFormat = Json.format[Seller]

}
