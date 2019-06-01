package services

import com.appodealx.exchange.settings.models.seller.Seller
import monix.eval.Task

import scala.language.postfixOps

trait SellerRepo[F[_]] {
  def findSeller(id: Long): F[Option[Seller]]
}
