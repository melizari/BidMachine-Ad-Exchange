package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class Video(
                mimes: List[String],
                minduration: Option[Int] = None,
                maxduration: Option[Int] = None,
                @ApiModelProperty(allowableValues = "range(1, 6)") protocols: Option[List[Int]] = None,
                w: Option[Int] = None,
                h: Option[Int] = None,
                startdelay: Option[Int] = None,
                @ApiModelProperty(allowableValues = "1,2") linearity: Option[Int] = None,
                @ApiModelProperty(allowableValues = "range(1, 16)") battr: Option[List[Int]] = None,
                maxextended: Option[Int] = None,
                minbitrate: Option[Int] = None,
                maxbitrate: Option[Int] = None,
                @ApiModelProperty(dataType = "java.lang.Boolean") boxingallowed: Option[Boolean] = Some(true),
                @ApiModelProperty(allowableValues = "1,2,3,4") playbackmethod: Option[List[Int]] = None,
                @ApiModelProperty(allowableValues = "1,2") delivery: Option[List[Int]] = None,
                @ApiModelProperty(allowableValues = "range(0, 7)") pos: Option[Int] = None,
                @ApiModelProperty(allowableValues = "range(1, 5)") api: Option[List[Int]] = None,
                ext: Option[String] = None
                )
