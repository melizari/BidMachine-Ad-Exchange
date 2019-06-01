package com.appodealx.exchange.settings.models.seller


trait AdSpace[A] {
  def id: Option[AdSpaceId]
  def sellerId: Option[Long]
  def title: Option[String]
  def displayManager: Option[String]
  def active: Boolean
  def debug: Boolean

  def adChannel: Option[Int]

  def interstitial: Boolean
  def reward: Boolean

  def distributionChannel: Option[String]

  def ad: A
}
