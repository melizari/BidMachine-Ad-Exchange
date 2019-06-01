package com.appodealx.exchange.settings.models.seller

final case class Format(w: Option[Int],
                        h: Option[Int],
                        wratio: Option[Int],
                        hratio: Option[Int],
                        wmin: Option[Int])
