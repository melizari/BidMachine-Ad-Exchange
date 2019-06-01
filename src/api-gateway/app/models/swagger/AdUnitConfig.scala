package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import play.api.libs.json.JsObject

@ApiModel
case class AdUnitConfig(id: String,
                        demandPartnerCode: String,
                        @ApiModelProperty(allowableValues = "banner,video,native") adType: String,
                        format: Option[Vector[Format]],
                        isInterstitial: Boolean,
                        isRewarded: Boolean,
                        @ApiModelProperty(dataType = "Object")customParams: JsObject)
