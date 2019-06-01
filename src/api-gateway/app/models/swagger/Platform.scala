package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class Platform(
                     @ApiModelProperty(allowableValues = "android,ios,amazon", required = true) value: String
                   )
