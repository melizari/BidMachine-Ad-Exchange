package com.appodealx.exchange.settings.persistance.seller.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.AppId
import com.appodealx.exchange.settings.models.seller.AdSpaceId

trait AdSpaceColumns { self: Table[_] =>

  def id = column[AdSpaceId]("id", O.PrimaryKey, O.AutoInc)
  def sellerId = column[Option[Long]]("seller_id")
  def title = column[Option[String]]("title")
  def displayManager = column[Option[String]]("display_manager")
  def active = column[Boolean]("active")
  def debug = column[Boolean]("debug", O.Default(false))
  def adChannel = column[Option[Int]]("ad_channel")
  def interstitial = column[Boolean]("interstitial", O.Default(false))
  def reward = column[Boolean]("reward", O.Default(false))
  def distributionChannel = column[Option[String]]("distribution_channel")

}
