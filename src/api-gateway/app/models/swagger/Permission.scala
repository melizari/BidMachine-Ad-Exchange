package models.swagger

import io.swagger.annotations.ApiModelProperty

case class Permission(userId: Long,
                      resourceId: Long,
                      @ApiModelProperty(allowableValues = "seller,agency", required = true) resourceType: String)