package com.appodealx.exchange.settings.persistance.seller.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.settings.models.seller.Seller

class Sellers(tag: Tag) extends Table[Seller](tag, "sellers") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def ks = column[Option[String]]("ks")
  def name = column[Option[String]]("name")
  def fee = column[Option[Double]]("fee")
  def active = column[Option[Boolean]]("active")

  def bcat = column[Option[List[String]]]("blocked_categories")
  def badv = column[Option[List[String]]]("blocked_advertisers")
  def bapp = column[Option[List[String]]]("blocked_apps")

  def * = (id.?, ks, name, fee, active, bcat, badv, bapp) <> ((Seller.apply _).tupled, Seller.unapply)

}

object Sellers extends TableQuery(new Sellers(_))
