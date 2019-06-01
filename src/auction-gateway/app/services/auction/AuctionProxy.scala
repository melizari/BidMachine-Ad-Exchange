package services.auction

import com.appodealx.exchange.common.models.auction.{Adm, Plc}
import models.Ad
import models.auction.AdRequest

import scala.concurrent.duration._
import cats.instances.list._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.{Monad, Parallel}

import scala.concurrent.duration.Duration

trait AuctionProxy[F[_]] {

  def perform[A: Adm, P: Plc](requests: List[AdRequest[P]], auctions: List[Auction[F]]): F[Option[Ad]]

  def call(url: String, timeout: Duration): F[Unit]

  def notify(urls: List[String], timeout: Duration = 500.millis)(implicit M: Monad[F], P: Parallel[F, F]): F[Unit] =
    urls.parTraverse(call(_, timeout)).map(_ => ())
}
