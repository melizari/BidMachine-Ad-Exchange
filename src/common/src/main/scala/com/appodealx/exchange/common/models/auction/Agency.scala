package com.appodealx.exchange.common.models.auction

case class Agency(id: Option[AgencyId],
                  title: String,
                  contactName: Option[String],
                  instantMessaging: Option[String],
                  phone: Option[String],
                  email: Option[String],
                  site: Option[String],
                  externalId: Option[AgencyExternalId],
                  active: Option[Boolean] = Some(false))
