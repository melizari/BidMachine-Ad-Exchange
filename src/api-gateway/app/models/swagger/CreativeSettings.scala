package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class CreativeSettings(maxUniqueCid: Int, maxUniqueCrid: Int)
