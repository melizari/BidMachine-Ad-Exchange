package com.appodealx.exchange.settings.persistance.seller.dao

import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.common.models.AppId
import com.appodealx.exchange.settings.persistance.seller.tables.VideoAdSpaces
import com.appodealx.exchange.settings.models.seller.{AdSpaceId, VideoAdSpace}
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}


class VideoAdSpaceDAO(protected val dbConfig: DatabaseConfig[PostgresProfile])
  extends HasDatabaseConfig[PostgresProfile] {

  import profile.api._

  def insertWithSellerId(sellerId: Long, adSpace: VideoAdSpace)(implicit ec: ExecutionContext): Future[VideoAdSpace] = db.run {
    val bas = adSpace.copy(id = None, sellerId = Some(sellerId))
    VideoAdSpaces.returning(VideoAdSpaces.map(_.id)).into((bas, id) => bas.copy(id = Some(id))) += bas
  }

  def findById(adSpaceId: AdSpaceId)(implicit ec: ExecutionContext): Future[Option[VideoAdSpace]] = db.run {
    VideoAdSpaces.filter(_.id === adSpaceId).result.headOption
  }

  def findBySellerId(sellerId: Long)(implicit ec: ExecutionContext): Future[Seq[VideoAdSpace]] = db.run {
    VideoAdSpaces.filter(_.sellerId === sellerId).sortBy(_.id).result
  }

  def update(adSpace: VideoAdSpace)(implicit ec: ExecutionContext): Future[Option[VideoAdSpace]] = db.run {
    VideoAdSpaces.filter(_.id === adSpace.id).update(adSpace).map(u => Option(adSpace).filter(_ => u > 0))
  }

  def updateActive(id: AdSpaceId, active: Boolean)(implicit ec: ExecutionContext): Future[Boolean] = db.run {
    VideoAdSpaces.filter(_.id === id).map(_.active).update(active).map(_ > 0)
  }

  def delete(adSpaceId: AdSpaceId)(implicit ec: ExecutionContext): Future[Boolean] = db.run {
    VideoAdSpaces.filter(_.id === adSpaceId).delete
  }.map(_ > 0)

}
