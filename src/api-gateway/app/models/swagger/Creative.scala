package models.swagger

import java.sql.Timestamp

import io.swagger.annotations.{ApiModel, ApiModelProperty}

@ApiModel
case class Creative(
                     id: Option[Long] = None,
                     cid: Option[String],
                     crid: Option[String],
                     adDomain: Option[String],
                     adCategory: Option[String],
                     agencyId: Option[Long],
                     agencyName: String,
                     iURL: Option[String],
                     hash: String,
                     imageURL: Option[String],
                     admURL: Option[String],
                     updated: Option[Timestamp],
                     @ApiModelProperty(allowableValues = "active, defected, violating") reviewReason: String
                   )
