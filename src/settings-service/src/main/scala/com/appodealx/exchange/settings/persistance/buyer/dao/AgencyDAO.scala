package com.appodealx.exchange.settings.persistance.buyer.dao

import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.appodealx.exchange.settings.persistance.buyer.tables.Agencies
import com.appodealx.exchange.common.models.auction.{Agency, AgencyExternalId, AgencyId}
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig

class AgencyDAO(protected val dbConfig: DatabaseConfig[PostgresProfile])
  extends HasDatabaseConfig[PostgresProfile]
  with DBIOActionSyntax {

  import profile.api._

  def findAll = db.run(Agencies.sortBy(_.id).result)

  def find(id: AgencyId) = Agencies.filter(_.id === id).result.headOption.toTask


  def insert(agency: Agency) = {
    val insertQuery = Agencies returning Agencies.map(_.id) into ((Agencies, id) => Agencies.copy(id = Some(id)))
    val act = insertQuery += agency.copy(id = None, externalId = None, active = Some(false))
    act.toTask
  }

  def update(agency: Agency) = {
    val action = Agencies.filter(_.id === agency.id).update(agency)
    action.toTask.map(u => Option(agency).filter(_ => u > 0))
  }

  def delete(id: AgencyId) = Agencies.filter(_.id === id).delete.toTask.map(_ > 0)

  def updateExtId(agencyId: AgencyId, extId: AgencyExternalId) = {
    val action = Agencies.filter(_.id === agencyId).map(a => a.extId).update(Some(extId))
    action.toTask.map(u => u > 0)
  }

  def updateActiveStatus(agencyId: AgencyId, isActive: Boolean) = {
    val action = Agencies.filter(_.id === agencyId).map(a => a.active).update(Some(isActive))
    action.toTask.map(u => u > 0)
  }

}