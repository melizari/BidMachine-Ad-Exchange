package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class Auth(token: String,
                expires: String)
