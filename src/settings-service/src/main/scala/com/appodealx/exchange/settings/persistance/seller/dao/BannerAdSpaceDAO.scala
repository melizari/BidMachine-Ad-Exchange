package com.appodealx.exchange.settings.persistance.seller.dao

import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.common.models.AppId
import com.appodealx.exchange.settings.persistance.seller.tables.BannerAdSpaces
import com.appodealx.exchange.settings.models.seller.{AdSpaceId, BannerAdSpace}
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}


class BannerAdSpaceDAO(protected val dbConfig: DatabaseConfig[PostgresProfile])
  extends HasDatabaseConfig[PostgresProfile] {

  import profile.api._

  def insertWithSellerId(sellerId: Long, adSpace: BannerAdSpace)(implicit ec: ExecutionContext): Future[BannerAdSpace] = db.run {
    val bas = adSpace.copy(id = None, sellerId = Some(sellerId))
    BannerAdSpaces returning BannerAdSpaces.map(_.id) into ((b, id) => b.copy(id = Some(id))) += bas
  }

  def findById(adSpaceId: AdSpaceId)(implicit ec: ExecutionContext): Future[Option[BannerAdSpace]] = db.run {
    BannerAdSpaces.filter(_.id === adSpaceId).result.headOption
  }

  def findBySellerId(sellerId: Long)(implicit ec: ExecutionContext): Future[Seq[BannerAdSpace]] = db.run {
    BannerAdSpaces.filter(_.sellerId === sellerId).sortBy(_.id).result
  }

  def update(adSpace: BannerAdSpace)(implicit ec: ExecutionContext): Future[Option[BannerAdSpace]] = db.run {
    BannerAdSpaces.filter(_.id === adSpace.id).update(adSpace).map(u => Option(adSpace).filter(_ => u > 0))
  }

  def updateActive(id: AdSpaceId, active: Boolean)(implicit ec: ExecutionContext): Future[Boolean] = db.run {
    BannerAdSpaces.filter(_.id === id).map(_.active).update(active).map(_ > 0)
  }

  def delete(adSpaceId: AdSpaceId)(implicit ec: ExecutionContext): Future[Boolean] = db.run {
    BannerAdSpaces.filter(_.id === adSpaceId).delete
  }.map(_ > 0)

}
