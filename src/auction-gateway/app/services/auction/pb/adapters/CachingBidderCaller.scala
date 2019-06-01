package services.auction.pb.adapters

import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import models.auction.NoBidReason.QueriesLimitExceeded
import models.auction.{AdRequest, AdUnit, AdapterResult, BiddingResult}
import play.api.Logger
import scalacache.{CacheAlg, Mode}

import cats.Monad
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

class CachingBidderCaller[F[_]: Monad: Mode](
  callForBids: BidderCaller[F],
  cache: CacheAlg[BiddingResult],
  ttl: FiniteDuration
) extends BidderCaller[F] {

  private val F      = Monad[F]
  private val logger = Logger(this.getClass)

  override def apply[P: Plc](req: AdRequest[P], bidder: Bidder, adUnits: List[AdUnit]): F[AdapterResult] = {

    def getAndPutInCache(key: String) =
      for {
        result <- callForBids(req, bidder, adUnits)
        _ <- result match {
              case (_, Left(QueriesLimitExceeded)) => F.pure[Any](())
              case (_, r)                          => cache.put(key)(r, Some(ttl))
            }
      } yield result

    def getBiddingResult(key: String) =
      for {
        fromCache <- cache.get(key)
        _         <- logCached(fromCache, bidder.protocol.value)
        result    <- fromCache.fold(getAndPutInCache(key))(cached)
      } yield result

    val key = req.impId.map(cacheKey(bidder.protocol.value, Plc[P].name))

    key match {
      case Some(k) => getBiddingResult(k)
      case None    => callForBids(req, bidder, adUnits)
    }
  }

  private def cached(result: BiddingResult) = (true -> result).pure[F]

  private def cacheKey(name: String, adTypeName: String)(id: String) = s"$name#$adTypeName#$id"

  private def logCached(fromCached: Option[BiddingResult], bidderName: String) = {
    if (fromCached.isDefined) logger.debug(s"$bidderName's cached value: ${fromCached.get}")
    ().pure[F]
  }
}
