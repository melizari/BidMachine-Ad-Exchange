package com.appodealx.exchange.settings.models.seller

import com.appodealx.exchange.common.models.dto.Native

case class NativeAdSpace(id: Option[AdSpaceId] = None,
                         sellerId: Option[Long] = None,
                         title: Option[String],
                         displayManager: Option[String],
                         active: Boolean,
                         debug: Boolean,
                         adChannel: Option[Int] = None,
                         interstitial: Boolean = false,
                         reward: Boolean,
                         distributionChannel: Option[String],
                         ad: Native)
    extends AdSpace[Native]
