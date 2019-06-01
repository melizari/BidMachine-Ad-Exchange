package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class Banner(
                  w: Option[Int],
                  h: Option[Int],
                  @ApiModelProperty(allowableValues = "range(1, 4)") btype: Option[List[Int]],
                  @ApiModelProperty(allowableValues = "range(1, 16)") battr: Option[List[Int]],
                  @ApiModelProperty(allowableValues = "range(0, 7)") pos: Option[Int],
                  mimes: Option[List[String]],
                  @ApiModelProperty(dataType = "java.lang.Boolean") topframe: Option[Boolean],
                  @ApiModelProperty(allowableValues = "range(1, 5)") expdir: Option[List[Int]],
                  @ApiModelProperty(allowableValues = "range(1, 5)") api: Option[List[Int]],
                  ext: Option[String] = None
                 )
