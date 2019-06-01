package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class LoginPass(login: String, password: String)
