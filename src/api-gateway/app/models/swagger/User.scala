package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class User(id: Option[Long],
                email: String,
                @ApiModelProperty(allowableValues = "admin,api,buyer", required = true) role: String,
                name: Option[String],
                company: Option[String],
                permissions: Vector[Permission])
