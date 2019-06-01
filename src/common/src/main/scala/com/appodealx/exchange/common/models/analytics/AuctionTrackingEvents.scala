package com.appodealx.exchange.common.models.analytics

import com.appodealx.exchange.common.models.Uri


case class AuctionTrackingEvents(loaded: Option[Uri] = None,
                                 impression: Option[Uri] = None,
                                 click: Option[Uri] = None,
                                 closed: Option[Uri] = None,
                                 error: Option[Uri] = None,
                                 trackingError: Option[Uri] = None,
                                 destroy: Option[Uri] = None,
                                 viewable: Option[Uri] = None)
