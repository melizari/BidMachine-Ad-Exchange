package com.appodealx.exchange.settings.persistance.seller.repos

import cats.syntax.option._
import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.appodealx.exchange.settings.persistance.seller.tables.Sellers
import com.appodealx.exchange.settings.models.seller.Seller
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig

import scala.concurrent.ExecutionContext

class SellerRepository(protected val dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext)
  extends HasDatabaseConfig[PostgresProfile] with DBIOActionSyntax {

  import profile.api._

  def findAll = Sellers.sortBy(_.id).result.run()

  def findOne(id: Long) = Sellers.filter(_.id === id).result.headOption.run()

  def findByKs(ks: String) = Sellers.filter(_.ks === ks).result.headOption.run()

  def insert(seller: Seller) = {
    def updateId(s: Seller, id: Long) = s.copy(id = id.some)
    val selectId = Sellers.map(_.id)

    (Sellers.returning(selectId).into(updateId) += seller.copy(id = None)).run()
  }

  def update(seller: Seller) = Sellers
    .filter(_.id === seller.id)
    .update(seller)
    .map(u => Option(seller).filter(_ => u > 0))
    .run()

  def delete(id: Long) = Sellers.filter(_.id === id).delete.map(_ => ()).run()
}
