package services.auction.pb.adapters.hangmyads

import com.appodealx.exchange.common.models.Failure
import com.appodealx.exchange.common.models.FailureReason.AdmPrepareFailure
import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import models.PbAd
import models.auction.{AdRequest, AdUnit, Bid}
import monix.eval.Task
import play.api.Logger
import play.twirl.api.Html
import services.auction.pb.DefaultAdapter
import services.auction.pb.adapters.BidderCaller

import cats.implicits._

class HangMyAdsAdapter(callForBids: BidderCaller[Task]) extends DefaultAdapter[Task]("hangmyads") {

  private val logger = Logger(this.getClass)

  override def announce[P: Plc](bidder: Bidder, request: AdRequest[P], adUnits: List[AdUnit]) =
    callForBids(request, bidder, adUnits)

  override def prepareAd[A: Adm](bid: Bid): Task[PbAd] =
    if (Adm[A].is[Html]) {

      (for {
        stringAdm <- bid.adm.toRight(Failure(AdmPrepareFailure, "HangMyAds: Bid has no adm"))
        rawMarkup <- Adm[A].parse(stringAdm)
        markup    = Adm[A].render(rawMarkup)
        trackers  = impTrackersOf(bid)
      } yield PbAd(markup, impTrackers = trackers)).liftTo[Task]

    } else {
      val message = s"HangMyAds: ${Adm[A]} adm type not supported yet"
      logger.error(message)
      Task.raiseError(new RuntimeException(message))
    }

  private def impTrackersOf(bid: Bid) =
    bid.ext
      .flatMap(_.hcursor.get[String]("imptrackers").toOption)
      .map(_ :: Nil)
      .getOrElse(Nil)

}
