package services.auction.pb

import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import models.PbAd
import models.auction.{AdRequest, AdUnit, AdapterResult, Bid}

trait Adapter[F[_]] {

  def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]): F[AdapterResult]

  def prepareAd[A: Adm](bid: Bid): F[PbAd]

}
