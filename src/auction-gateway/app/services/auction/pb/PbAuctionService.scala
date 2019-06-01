package services.auction.pb

import com.appodealx.exchange.common.models.auction.{Adm, Plc}
import models.auction.AdUnit
import models.{Ad, RequestHost, SdkRequest}

trait PbAuctionService[F[_]] {
  def perform[A: Adm, P: Plc](
    sdkReq: SdkRequest,
    info: List[AdUnit],
    host: RequestHost,
    tmax: Option[Int]
  ): F[Option[Ad]]
}
