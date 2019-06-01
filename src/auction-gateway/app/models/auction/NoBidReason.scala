package models.auction

import com.appodealx.exchange.common.models.rtb._
import com.appodealx.openrtb

sealed trait NoBidReason extends Serializable with Product {
  def prettyValue: String
}

object NoBidReason {
  case object NoFill               extends NoBidReason { val prettyValue = "no_fill"                }
  case object Timeout              extends NoBidReason { val prettyValue = "timeout"                }
  case object QueriesLimitExceeded extends NoBidReason { val prettyValue = "queries_limit_exceeded" }

  case class ParsingError(message: String)   extends NoBidReason { val prettyValue = s"parsing_error_$message"      }
  case class UnexpectedResponse(status: Int) extends NoBidReason { val prettyValue = s"unexpected_response_$status" }
  case class RtbNoBidReason(nbr: openrtb.NoBidReason) extends NoBidReason {
    val prettyValue = s"rtb_nbr_${nbr.prettyValue}"
  }
  case class RequestException(message: String) extends NoBidReason {
    val prettyValue = s"request_exception_$message"
  }
  case object BelowPriceFloor extends NoBidReason { val prettyValue = "bid_below_price_floor" } // Remove

}
