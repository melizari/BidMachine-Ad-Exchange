package com.appodealx.exchange.common.models.analytics

import com.appodealx.openrtb.{BidRequest, BidResponse}
import org.joda.time.DateTime

case class DebugInfo(timestamp: DateTime,
                     bidRequest: BidRequest,
                     bidResponse: Option[BidResponse] = None,
                     responseFailedStatus: Option[String] = None)