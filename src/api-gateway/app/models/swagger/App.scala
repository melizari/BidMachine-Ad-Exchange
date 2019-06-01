package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class App(id: String,
               @ApiModelProperty(allowableValues = "android,ios", required = true) platform: String,
               bundle: String,
               name: Option[String],
               domain: Option[String],
               storeurl: Option[String],
               storeid: Option[String],
               cat: Option[List[String]] = None,
               @ApiModelProperty(dataType = "java.lang.Boolean") privacypolicy: Option[Boolean],
               @ApiModelProperty(dataType = "java.lang.Boolean") paid: Option[Boolean],
               publisher: Option[Publisher] = None,
               keywords: Option[String] = None,
               settings: Option[AppSettings])
