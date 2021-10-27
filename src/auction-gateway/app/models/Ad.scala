package models

import com.appodealx.exchange.common.models.{Markup, Size}
import com.appodealx.exchange.common.models.analytics.AuctionTrackingEvents
import models.auction.Metadata


case class Ad(markup: Markup,
              size: Option[Size],
              metadata: Metadata,
              trackingEvents: AuctionTrackingEvents,
              sspIncome: Double,
              adomain: Option[List[String]] = None,
              bundle: Option[String] = None,
              cat: Option[List[String]] = None,
              nurl: Option[String] = None,
              iurl: Option[String] = None
             )
