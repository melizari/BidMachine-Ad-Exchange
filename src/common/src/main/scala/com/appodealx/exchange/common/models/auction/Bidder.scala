package com.appodealx.exchange.common.models.auction

import akka.http.scaladsl.model.Uri
import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.models.auction.AuctionType.SecondPrice
import com.appodealx.exchange.common.models.rtb.Version

case class Bidder(id: Option[BidderId] = None,
                  agencyId: AgencyId,
                  auctionType: AuctionType = SecondPrice,
                  title: String,
                  endpoint: Uri,
                  rtbVersion: Version,
                  coppaFlag: Boolean,
                  worldwide: Boolean,
                  countries: Option[List[String]],
                  platforms: List[Platform],
                  protocol: Protocol,
                  maxRpm: Long,
                  adControl: Option[Boolean],
                  excludedSellers: Option[List[Long]],
                  includedSellers: Option[List[Long]])
