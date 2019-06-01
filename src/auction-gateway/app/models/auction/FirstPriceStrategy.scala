package models.auction

import play.api.Logger

import scala.util.Random

object FirstPriceStrategy extends Strategy {

  private val logger = Logger(getClass)


  def execute[B: HasPrice](startingPrice: Double, bids: List[B]) = {

    import HasPrice.syntax._

    logger.debug(s"Starting first price auction with $startingPrice starting price and ${bids.length} bids...")

    val shuffledBids = Random.shuffle(bids)
    val sortedBids = shuffledBids.sortBy(_.price)(Ordering[Double].reverse)

    val prices = sortedBids.map(_.price)

    val result = prices.zip(sortedBids)

    if (result.nonEmpty) {
      logger.debug(s"Winner found with clearingPrice ${result.head._1}")
    } else {
      logger.debug(s"No winner found!")
    }

    result
  }

}
