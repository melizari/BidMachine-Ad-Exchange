package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class BannerAdSpace(id: Option[Long] = None,
                         appId: Option[Long] = None,
                         sellerId: Option[Long] = None,
                         title: Option[String],
                         displayManager: Option[String],
                         active: Boolean,
                         debug: Boolean,
                         adChannel: Option[Int] = None,
                         interstitial: Boolean = false,
                         distributionChannel: Option[String],
                         ad: Banner)