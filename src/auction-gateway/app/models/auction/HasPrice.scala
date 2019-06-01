package models.auction

import cats.Comonad
import cats.syntax.comonad._

trait HasPrice[B] {
  def price(b: B): Double
}

object HasPrice {

  import syntax._

  def apply[B](implicit b: HasPrice[B]) = b

  def instance[B](f: B => Double): HasPrice[B] = (b: B) => f(b)

  implicit val bidHasPrice = HasPrice.instance[Bid](_.price)

  //implicit val auctionBidHasPrice = HasPrice.instance[MetaBid](_.price)

  implicit def comonadHasPrice[F[_]: Comonad, B: HasPrice]: HasPrice[F[B]] = HasPrice.instance(_.extract.price)

  object syntax {

    implicit class HasPriceOps[B: HasPrice](b: B) {
      def price: Double = HasPrice[B].price(b)
    }

  }

}
