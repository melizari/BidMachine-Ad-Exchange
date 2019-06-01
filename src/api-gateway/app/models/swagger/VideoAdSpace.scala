package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class VideoAdSpace(id: Option[Long],
                        appId: Option[Long],
                        sellerId: Option[Long],
                        title: Option[String],
                        displayManager: Option[String],
                        active: Boolean,
                        debug: Boolean,
                        adChannel: Option[Int] = None,
                        interstitial: Boolean,
                        distributionChannel: Option[String],
                        ad: Video)
