package com.appodealx.openrtb

case class BidResponse(id: String,
                       seatbid: Option[List[SeatBid]] = None,
                       bidid: Option[String] = None,
                       cur: Option[String] = Some("USD"),
                       customdata: Option[String] = None,
                       nbr: Option[NoBidReason] = None,
                       ext: Option[Json] = None)
