package com.appodealx.exchange.settings.persistance.buyer.dao

import cats.data.OptionT
import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.appodealx.exchange.settings.persistance.buyer.tables.{Agencies, BannerAdProfiles, Bidders, EnumMappingInstances, ListMappingInstances}
import com.appodealx.exchange.common.models.auction.{AdProfileId, BidderId}
import com.appodealx.exchange.settings.models.buyer.BannerAdProfile
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig
import monix.eval.Task

class BannerAdProfileDAO(protected val dbConfig: DatabaseConfig[PostgresProfile])
  extends AdProfileDAO[BannerAdProfile]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax
    with ListMappingInstances
    with EnumMappingInstances {

  import profile.api._

  def insert(profile: BannerAdProfile) = {
    val withDefaults = profile.copy(ad = profile.ad.withDefaults, active = false)
    BannerAdProfiles.returning(BannerAdProfiles.map(_.id)).into((p, id) => p.copy(id = Some(id))) += withDefaults
  }.toTask

  def find(id: AdProfileId) = BannerAdProfiles.filter(_.id === id).result.headOption.toTask

  def findAll = {
    (for {
      p <- BannerAdProfiles
      b <- Bidders.filter(_.id === p.bidderId)
    } yield (p, b)).sortBy(_._1.id).result.toTask
  }

  def findByBidderId(bidderId: BidderId) = BannerAdProfiles.filter(_.bidderId === bidderId).result.toTask

  def update(profile: BannerAdProfile) = {
    {
      for {
        id <- OptionT(Task.now(profile.id))
        foundProfile <- OptionT(find(id))
        updatedProfile <- OptionT(BannerAdProfiles
          .filter(_.id === profile.id)
          .update(profile.copy(bidderId = foundProfile.bidderId))
          .toTask
          .map(u => Option(profile).filter(_ => u > 0)))
      } yield updatedProfile
    }.value
  }

  def delete(id: AdProfileId) = {
    BannerAdProfiles.filter(_.id === id).delete.toTask.map(_ > 0)
  }

  def updateActive(id: AdProfileId, active: Boolean): Task[Boolean] = {
    BannerAdProfiles.filter(_.id === id).map(p => p.active).update(active).toTask.map(_ > 0)
  }

}
