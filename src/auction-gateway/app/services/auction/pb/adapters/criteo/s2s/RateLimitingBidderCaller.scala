package services.auction.pb.adapters.criteo.s2s

import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import models.auction.NoBidReason.QueriesLimitExceeded
import models.auction.{AdRequest, AdUnit, AdapterResult, Bid, BiddingResult, NoBidReason}
import redis.RedisClient
import services.auction.pb.adapters.BidderCaller

import cats.Monad
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.either._
import cats.syntax.functor._

class RateLimitingBidderCaller[F[_]](
  serviceName: String,
  redisClient: RedisClient,
)(callForBids: BidderCaller[F])(implicit E: Execute[F], M: Monad[F])
    extends BidderCaller[F] {

  private val nbr: BiddingResult = QueriesLimitExceeded.asLeft

  override def apply[P: Plc](req: AdRequest[P], bidder: Bidder, adUnits: List[AdUnit]): F[AdapterResult] =
    for {
      rqNumber <- E.deferFuture(redisClient.incr(serviceName))
      _        <- if (rqNumber == 1) E.deferFuture(redisClient.expire(serviceName, 1)) else false.pure[F]
      sb       <- if (rqNumber < bidder.maxRpm) callForBids(req, bidder, adUnits) else (false, nbr).pure[F]
    } yield sb
}
