package com.appodealx.exchange.settings.persistance.buyer.tables

import akka.http.scaladsl.model.Uri
import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.auction._
import com.appodealx.exchange.common.models.rtb.Version
import com.appodealx.exchange.common.models.{Country, Platform}

import scala.language.implicitConversions


object Bidders extends TableQuery(new Bidders(_)) {

  implicit val countriesListMapping = {
    implicit val dummyArrChecked = PostgresProfile.ElemWitness.AnyWitness.asInstanceOf[PostgresProfile.ElemWitness[Country]]
    new PostgresProfile.SimpleArrayJdbcType[Country]("text").to(_.toList)
  }

}

class Bidders(tag: Tag) extends Table[Bidder](tag, "bidder")
  with EnumMappingInstances {

  def id = column[BidderId]("id", O.PrimaryKey, O.AutoInc)
  def agencyId = column[AgencyId]("agency_id")
  def auctionType = column[AuctionType]("auction_type")
  def title = column[String]("title")
  def endpoint = column[Uri]("endpoint")
  def rtbVersion = column[Version]("rtb_version")
  def coppaFlag = column[Boolean]("coppa_flag")
  def worldwide = column[Boolean]("worldwide")
  def countries = column[Option[List[String]]]("countries")
  def platforms = column[List[Platform]]("platforms")
  def protocol = column[Protocol]("protocol")
  def maxRpm = column[Long]("max_rpm")
  def agency = foreignKey("agency_fk", agencyId, Agencies)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def adControl = column[Option[Boolean]]("ad_control")
  def excludedSellers = column[Option[List[Long]]]("excluded_sellers")
  def includedSellers = column[Option[List[Long]]]("included_sellers")

  def * = (
    id.?,
    agencyId,
    auctionType,
    title,
    endpoint,
    rtbVersion,
    coppaFlag,
    worldwide,
    countries,
    platforms,
    protocol,
    maxRpm,
    adControl,
    excludedSellers,
    includedSellers
  ) <> ((Bidder.apply _).tupled, Bidder.unapply)
}