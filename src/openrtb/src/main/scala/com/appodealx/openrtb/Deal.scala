package com.appodealx.openrtb

case class Deal(id: String,
                bidfloor: Option[Double] = Some(0.0),
                bidfloorcur: Option[String] = Some("USD"),
                at: Option[AuctionType] = None,
                wseat: Option[List[String]] = None,
                wadomain: Option[List[String]] = None,
                ext: Option[Json] = None)
