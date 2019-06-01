package models.swagger

import io.swagger.annotations.ApiModelProperty

case class AppSettings(@ApiModelProperty("blocked bidder ids") blockedBidders: Option[Vector[String]],
                       @ApiModelProperty("blocked IAB categories") blockedCategories: Option[Vector[String]],
                       @ApiModelProperty("blocked advertiser domains") blockedAdvertisers: Option[Vector[String]])
