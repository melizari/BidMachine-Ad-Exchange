package com.appodealx.exchange.settings.persistance.buyer.dao

import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.settings.persistance.buyer.tables.Bidders
import com.appodealx.exchange.common.models.auction.{AgencyId, Bidder, BidderId}
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}


class BidderDAO(protected val dbConfig: DatabaseConfig[PostgresProfile])
  extends HasDatabaseConfig[PostgresProfile] {

  import profile.api._

  def findAll(implicit ec: ExecutionContext): Future[Seq[Bidder]] = db.run(Bidders.sortBy(_.id).result)

  def find(id: BidderId)(implicit ec: ExecutionContext): Future[Option[Bidder]] = db.run {
    Bidders.filter(_.id === id).result.headOption
  }

  def findByAgencyId(agencyId: AgencyId)(implicit ec: ExecutionContext): Future[Seq[Bidder]] = db.run {
    Bidders.filter(_.agencyId === agencyId).sortBy(_.id).result
  }

  def insert(bidder: Bidder)(implicit ec: ExecutionContext): Future[Bidder] = db.run {
    Bidders returning Bidders.map(_.id) into ((b, id) => b.copy(id = Some(id))) += bidder.copy(id = None)
  }

  def delete(id: BidderId)(implicit ec: ExecutionContext): Future[Boolean] = db.run {
    Bidders.filter(_.id === id).delete.map(_ > 0)
  }

  def update(bidder: Bidder)(implicit ec: ExecutionContext): Future[Option[Bidder]] = db.run {
    Bidders.filter(_.id === bidder.id).update(bidder).map(u => Option(bidder).filter(_ => u > 0))
  }

}
