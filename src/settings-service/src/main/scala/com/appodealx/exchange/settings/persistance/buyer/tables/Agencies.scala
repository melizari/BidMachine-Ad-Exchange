package com.appodealx.exchange.settings.persistance.buyer.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.auction.{Agency, AgencyExternalId, AgencyId}


class Agencies(tag: Tag) extends Table[Agency](tag, "agency") {

  def id = column[AgencyId]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def contactName = column[Option[String]]("contact_name")
  def instantMessaging = column[Option[String]]("instant_messaging")
  def phone = column[Option[String]]("phone")
  def email = column[Option[String]]("email")
  def site = column[Option[String]]("site")
  def extId = column[Option[AgencyExternalId]]("ext_id")
  def active = column[Option[Boolean]]("active", O.Default(Some(false)))

  def bidders = Bidders.filter(_.agencyId === id)

  def * = (
    id.?,
    title,
    contactName,
    instantMessaging,
    phone,
    email,
    site,
    extId,
    active) <> ((Agency.apply _).tupled, Agency.unapply)

}

object Agencies extends TableQuery(new Agencies(_))