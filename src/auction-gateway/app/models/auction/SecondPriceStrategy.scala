package models.auction

import play.api.Logger

import scala.util.Random


object SecondPriceStrategy extends Strategy {

  import HasPrice.syntax._

  private val logger = Logger(getClass)

  private val PRICE_MARGIN = 0.01

  private def marginPrice[B: HasPrice](clearingPrice: Double, bid: B) = {
    val priceWithMargin = (clearingPrice + PRICE_MARGIN).roundCPM
    Math.min(bid.price, priceWithMargin)
  }


  def execute[B: HasPrice](startingPrice: Double, bids: List[B]) = {

    logger.debug(s"Starting second price auction with $startingPrice starting price and ${bids.length} bids...")

    val shuffledBids = Random.shuffle(bids)
    val sortedBids = shuffledBids.sortBy(_.price)(Ordering[Double].reverse)

    val prices = sortedBids.map(_.price)
    val shiftedPrices = prices.drop(1) :+ startingPrice

    val result = shiftedPrices.zip(sortedBids).map { case (p, b) => (marginPrice(p, b), b) }

    if (result.nonEmpty) {
      logger.debug(s"Winner found with clearingPrice ${result.head._1}")
    } else {
      logger.debug(s"No winner found!")
    }

    result
  }

}