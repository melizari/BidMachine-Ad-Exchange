package models.swagger

import io.swagger.annotations.ApiModel

@ApiModel
case class Agency(
                   id: Option[Long],
                   title: String,
                   contactName: Option[String],
                   instantMessaging: Option[String],
                   phone: Option[String],
                   email: Option[String],
                   site: Option[String],
                   externalId: Option[Long],
                   active: Option[Boolean] = Some(false)
                 )