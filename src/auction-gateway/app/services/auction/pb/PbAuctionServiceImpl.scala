package services.auction.pb

import com.appodealx.exchange.common.models.auction.{Adm, Plc}
import kamon.AdRequestMetrics
import models.auction.AdUnit
import models.{Ad, MarketplaceType, RequestHost, SdkRequest}
import services.auction.{Auction, AuctionProxy}

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._

class PbAuctionServiceImpl[F[_]](auction: AuctionProxy[F],
                                 auctionsPerType: Map[MarketplaceType, Auction[F]],
                                 defaultAuctions: List[Auction[F]],
                                 adRequestBuilder: PbAdRequestsBuilder[F])(implicit M: MonadError[F, Throwable])
    extends PbAuctionService[F] {

  override def perform[A: Adm, P: Plc](
    sdkReq: SdkRequest,
    info: List[AdUnit],
    host: RequestHost,
    tmax: Option[Int]
  ): F[Option[Ad]] = {

    def auctionsToPerform =
      sdkReq.marketplaces.map { types =>
        auctionsPerType.filterKeys(t1 => types.exists(t1 == _)).values
      }.getOrElse(defaultAuctions).toList

    for {
      rq <- adRequestBuilder.buildRequests[P](sdkReq, info, host, tmax)
      _  <- AdRequestMetrics.measureMetrics[F, P](rq.head, pb = true)
      ad <- auction.perform[A, P](rq, auctionsToPerform)
    } yield ad
  }
}
