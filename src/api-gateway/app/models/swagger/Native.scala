package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class Native(ver: Option[String] = None,
                  @ApiModelProperty(allowableValues = "range(1, 5)") api: Option[List[Int]] = None,
                  @ApiModelProperty(allowableValues = "range(1, 16)") battr: Option[List[Int]] = None,
                  ext: Option[String] = None)
