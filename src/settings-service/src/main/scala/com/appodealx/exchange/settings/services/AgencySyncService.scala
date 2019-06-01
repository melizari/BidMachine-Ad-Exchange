package com.appodealx.exchange.settings.services

import com.appodealx.exchange.common.models.auction._
import com.appodealx.exchange.settings.models.buyer.{ExternalAgencyCreateRequest, ExternalAgencyUpdateRequest}
import com.appodealx.exchange.settings.persistance.buyer.dao.AgencyDAO
import monix.eval.Task

trait AgencySyncService {

  /**
   * Create agency on remote and update external id in agency
   *
   * @param agency agency
   * @return true is success, false if failure
   */
  def createAgency(agency: Agency): Task[Boolean]

  /**
   * Update agency on remote with new title
   *
   * @param agency agency
   * @return true is success, false if failure
   */
  def updateAgency(agency: Agency): Task[Boolean]

}

class AgencySyncServiceImpl(externalAgencyService: AgencyExternalService, agencyDAO: AgencyDAO)
    extends AgencySyncService {

  def createAgency(agency: Agency): Task[Boolean] =
    if (agency.externalId.isDefined) {
      Task.pure(true)
    } else {
      externalAgencyService.createExtAgency(ExternalAgencyCreateRequest(agency.id.get, agency.title)).flatMap {
        case Some(id) => agencyDAO.updateExtId(agency.id.get, id)
        case None     => Task.pure(false)
      }
    }

  def updateAgency(agency: Agency): Task[Boolean] =
    if (agency.externalId.isDefined) {
      externalAgencyService.updateExtAgency(ExternalAgencyUpdateRequest(agency.externalId.get, agency.title))
    } else {
      Task.pure(false)
    }
}
