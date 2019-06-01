package models.auction

trait Strategy extends {

  def execute[B: HasPrice](startingPrice: Double, bids: List[B]): List[(Double, B)]

}