package com.appodealx.exchange.common.models

import com.appodealx.openrtb

case class App(id: Option[AppId],
               externalId: Option[String],
               publisherId: PublisherId,
               openRtbApp: openrtb.App)
