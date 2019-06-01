package com.appodealx.exchange.settings.models.buyer

import com.appodealx.exchange.common.models.auction.{AgencyExternalId, AgencyId}

/**
  * Request to appodeal service for create system campaign
  * Request: '{"title":"TestTitleHere"}'
  *
  */
case class ExternalAgencyCreateRequest(agencyId: AgencyId, title: String)


/**
  * Request to appodeal service for update system campaign
  * Request: '{"id": 123, "title":"new title here"}'
  *
  * @param id    id of external Agency
  * @param title new title for Agency
  */
case class ExternalAgencyUpdateRequest(id: AgencyExternalId, title: String)


/**
  * Response from appodeal service after success create system campaign
  * Response: '{"campaign_id":123}'
  *
  * @param `campaign_type_id` id of external Agency
  */
case class ExternalAgencyCreateResponse(`campaign_type_id`: AgencyExternalId)


/**
  * Status response from appodeal services.
  */
case class AppodealResponseStatus(status: Int, success: Option[String] = None, error: Option[String] = None)
