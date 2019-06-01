package com.appodealx.openrtb

case class SeatBid(bid: List[Bid],
                   seat: Option[String] = None,
                   group: Option[Int] = Some(0),
                   ext: Option[Json] = None)