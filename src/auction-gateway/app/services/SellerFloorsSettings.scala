package services

import play.api.ConfigLoader.configLoader
import org.parboiled2._
import services.SellerFloorsSettings.SellerId

import scala.util.Random

/**
 * Custom Bid Floors for seller settings. Must be moved to (Business Setting Storage in future)
 *
 * @param floorsBySeller String in format "<seller_a>:<bid_floor_a_1>,...,<bid_floor_a_n>;<seller_b>:<bid_floor_b_1>,...,<bid_floor_b_m>"
 */
final case class SellerFloorsSettings(floorsBySeller: Map[SellerId, List[Double]], limitBySeller: Map[SellerId, Int]) {
  def getBidFloorsBySellerId(id: SellerId): Option[List[Double]] = {
    for {
      floors <- floorsBySeller.get(id)
      limit  = limitBySeller.getOrElse(id, Int.MaxValue)
    } yield
      if (floors.length > limit)
        Random.shuffle(floors).take(limit).sorted(Ordering[Double].reverse)
      else
        floors.sorted(Ordering[Double].reverse)
  }
}

object SellerFloorsSettings {

  type SellerId = Long

  class FloorConfigParser(val input: ParserInput) extends Parser {
    import FloorConfigParser._

    def WS(c: Char)    = rule { c ~ WhiteSpace }
    def WhiteSpace     = rule { zeroOrMore(WhiteSpaceChar) }
    def Digits         = rule { oneOrMore(CharPredicate.Digit) }
    def Integer        = rule { optional('-') ~ (CharPredicate.Digit19 ~ Digits | CharPredicate.Digit) }
    def Frac           = rule { "." ~ Digits }
    def Id             = rule { capture(Integer) ~> (_.toLong) ~ WhiteSpace }
    def Floor          = rule { capture(Integer ~ optional(Frac)) ~> (_.toDouble) ~ WhiteSpace }
    def Floors         = rule { zeroOrMore(Floor).separatedBy(WS(',')) ~> (_.toList) }
    def Limit          = rule { capture(Integer) ~> (_.toInt) ~ WhiteSpace }
    def SellerFloors   = rule { Id ~ WS(':') ~ Floors ~> (_ -> _) }
    def SellerLimits   = rule { Id ~ WS(':') ~ Limit ~> (_ -> _) }
    def FloorSettings  = rule { zeroOrMore(SellerFloors).separatedBy(WS(';')) ~> (_.toMap) }
    def LimitsSettings = rule { zeroOrMore(SellerLimits).separatedBy(WS(';')) ~> (_.toMap) }
  }

  object FloorConfigParser {
    val WhiteSpaceChar = CharPredicate(" \n\r\t\f")
  }

  implicit val confLoader = configLoader.map { conf =>
    val floorsString = conf.getString("bid-floors")
    val limitsString = conf.getString("limits")

    val floors = new FloorConfigParser(floorsString).FloorSettings.run()
      .getOrElse(Map.empty[Long, List[Double]])

    val limits = new FloorConfigParser(limitsString).LimitsSettings.run()
      .getOrElse(Map.empty[Long, Int])

    SellerFloorsSettings(floors, limits)
  }
}
