package com.appodealx.exchange.settings.persistance.buyer.dao

import cats.data.OptionT
import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.appodealx.exchange.settings.persistance.buyer.tables.{Agencies, Bidders, EnumMappingInstances, ListMappingInstances, NativeAdProfiles}
import com.appodealx.exchange.common.models.auction.{AdProfileId, BidderId}
import com.appodealx.exchange.settings.models.buyer.NativeAdProfile
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig
import monix.eval.Task

class NativeAdProfileDAO(protected val dbConfig: DatabaseConfig[PostgresProfile])
  extends AdProfileDAO[NativeAdProfile]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax
    with ListMappingInstances
    with EnumMappingInstances {

  import profile.api._

  def insert(profile: NativeAdProfile) = {
    val withDefaults = profile.copy(ad = profile.ad.withDefaults)
    NativeAdProfiles.returning(NativeAdProfiles.map(_.id)).into((p, id) => p.copy(id = Some(id))) += withDefaults
  }.toTask

  def findAll ={
    (for {
      p <- NativeAdProfiles
      b <- Bidders.filter(_.id === p.bidderId)
    } yield (p, b)).sortBy(_._1.id).result.toTask
  }

  def find(id: AdProfileId) = {
    NativeAdProfiles.filter(_.id === id).result.headOption.toTask
  }

  def findByBidderId(bidderId: BidderId) = {
    NativeAdProfiles.filter(_.bidderId === bidderId).result.toTask
  }

  def update(profile: NativeAdProfile) = {
    {
      for {
        id <- OptionT(Task.now(profile.id))
        foundProfile <- OptionT(find(id))
        updatedProfile <- OptionT(NativeAdProfiles
          .filter(_.id === profile.id)
          .update(profile.copy(bidderId = foundProfile.bidderId))
          .toTask
          .map(u => Option(profile).filter(_ => u > 0)))
      } yield updatedProfile
    }.value
  }

  def delete(id: AdProfileId) = {
    NativeAdProfiles.filter(_.id === id).delete.toTask
  }.map(_ > 0)

  def updateActive(id: AdProfileId, active: Boolean): Task[Boolean] = {
    NativeAdProfiles.filter(_.id === id).map(p => p.active).update(active).toTask
  }.map(_ > 0)

}
