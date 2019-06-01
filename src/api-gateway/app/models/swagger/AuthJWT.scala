package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class AuthJWT(token: String)
