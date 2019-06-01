package models.swagger

import io.swagger.annotations.{ApiModel, ApiModelProperty}
import org.joda.time.Interval

@ApiModel
case class RequestPerformance(interval: Option[Interval] = None,
                              granularity: Option[String] = Some("day"),
                              country: Option[List[String]] = None,
                              app: Option[List[String]] = None,
                              @ApiModelProperty(allowableValues = "banner,mrec,interstitial,native,skippable_video,non_skippable_video")adType: Option[List[String]] = None,
                              platform: Option[List[Platform]] = None,
                              direction: Option[String] = None,
                              agency: Option[List[String]] = None)