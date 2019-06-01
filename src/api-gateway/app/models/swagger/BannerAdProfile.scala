package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class BannerAdProfile(
                            id: Option[Long] = None,
                            bidderId: Long,
                            title: Option[String],
                            active: Boolean,
                            debug: Boolean,
                            adChannel: Option[Int] = None,
                            delayedNotification: Boolean,
                            interstitial: Boolean,
                            ad: Banner,
                            dmVerMax: Option[Version],
                            dmVerMin: Option[Version],
                            distributionChannel: Option[String]
                          )
