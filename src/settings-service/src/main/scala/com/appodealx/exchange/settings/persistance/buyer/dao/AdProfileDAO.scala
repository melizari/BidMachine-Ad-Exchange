package com.appodealx.exchange.settings.persistance.buyer.dao

import com.appodealx.exchange.common.models.auction._
import monix.eval.Task


abstract class AdProfileDAO[P <: AdProfile] {

  def insert(profile: P): Task[P]

  def find(id: AdProfileId): Task[Option[P]]

  def findByBidderId(bidderId: BidderId): Task[Seq[P]]

  def delete(id: AdProfileId): Task[Boolean]

  def updateActive(id: AdProfileId, active: Boolean): Task[Boolean]

}
