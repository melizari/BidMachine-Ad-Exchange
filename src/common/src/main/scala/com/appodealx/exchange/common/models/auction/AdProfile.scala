package com.appodealx.exchange.common.models.auction

import com.github.zafarkhaja.semver.Version

trait AdProfile {
  type Repr

  def id: Option[AdProfileId]
  def bidderId: BidderId
  def title: Option[String]
  def active: Boolean
  def debug: Boolean
  def adChannel: Option[Int]
  def delayedNotification: Boolean
  def interstitial: Boolean
  def reward: Boolean
  def ad: Repr
  def dmVerMin: Option[Version]
  def dmVerMax: Option[Version]
  def distributionChannel: Option[String]
  def allowCache: Option[Boolean]
  def allowCloseDelay: Option[Int]
}
