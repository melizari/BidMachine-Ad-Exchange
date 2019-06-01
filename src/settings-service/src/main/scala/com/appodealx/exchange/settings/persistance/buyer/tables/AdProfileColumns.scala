package com.appodealx.exchange.settings.persistance.buyer.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.auction.{AdProfileId, BidderId}
import com.github.zafarkhaja.semver.Version


trait AdProfileColumns { self: Table[_] =>

  def id = column[AdProfileId]("id", O.PrimaryKey, O.AutoInc)
  def bidderId = column[BidderId]("bidder_id")
  def title = column[Option[String]]("title")
  def active = column[Boolean]("active", O.Default(false))
  def debug = column[Boolean]("debug", O.Default(false))
  def adChannel = column[Option[Int]]("ad_channel")
  def delayedNotification = column[Boolean]("delayed_notification", O.Default(true))
  def interstitial = column[Boolean]("interstitial", O.Default(false))
  def reward = column[Boolean]("reward", O.Default(false))
  def dmVerMax = column[Option[Version]]("dm_ver_max")
  def dmVerMin = column[Option[Version]]("dm_ver_min")
  def distributionChannel = column[Option[String]]("distribution_channel")
  def template = column[Option[String]]("template")
  def allowCache = column[Option[Boolean]]("allow_cache")
  def allowCloseDelay = column[Option[Int]]("allow_close_delay")

  def bidder = foreignKey("bidder_fk", bidderId, Bidders)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

}