package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class Bidder(id: Option[Long] = None,
                  agencyId: Long,
                  title: String,
                  endpoint: Uri,
                  @ApiModelProperty(allowableValues = "23,24", required = true) rtbVersion: Int,
                  coppaFlag: Boolean,
                  worldwide: Boolean,
                  countries: Option[List[String]],
                  platforms: List[Platform],
                  maxRpm: Long,
                  adControl: Boolean,
                  excludedSellers: Option[List[Long]],
                  includedSellers: Option[List[Long]])
