package models.swagger

import io.swagger.annotations.ApiModelProperty

case class Format(@ApiModelProperty(dataType = "Integer")w: Option[Int],
                  @ApiModelProperty(dataType = "Integer")h: Option[Int],
                  @ApiModelProperty(dataType = "Integer")wratio: Option[Int],
                  @ApiModelProperty(dataType = "Integer")hratio: Option[Int],
                  @ApiModelProperty(dataType = "Integer")wmin: Option[Int])
