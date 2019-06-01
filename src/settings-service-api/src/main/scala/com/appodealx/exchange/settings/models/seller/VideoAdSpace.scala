package com.appodealx.exchange.settings.models.seller

import com.appodealx.exchange.common.models.dto.Video

case class VideoAdSpace(id: Option[AdSpaceId],
                        sellerId: Option[Long],
                        title: Option[String],
                        displayManager: Option[String],
                        active: Boolean,
                        debug: Boolean,
                        adChannel: Option[Int] = None,
                        interstitial: Boolean,
                        reward: Boolean,
                        distributionChannel: Option[String],
                        ad: Video)
    extends AdSpace[Video]
