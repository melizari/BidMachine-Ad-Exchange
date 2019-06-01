package services.auction.pb.adapters.criteo.s2s

import com.appodealx.exchange.common.models.auction.{Adm, Bidder, Plc}
import com.appodealx.exchange.common.models.{Failure, FailureReason}
import models.PbAd
import models.auction.{AdRequest, AdUnit, Bid}
import monix.eval.Task
import play.api.Logger
import play.twirl.api.Html
import services.auction.pb.DefaultAdapter
import services.auction.pb.adapters.BidderCaller

import cats.syntax.either._

class CriteoAdapter(callForBids: BidderCaller[Task]) extends DefaultAdapter[Task]("criteos2s") {

  private val logger = Logger(this.getClass)

  override def announce[P: Plc](bidder: Bidder, adRequest: AdRequest[P], info: List[AdUnit]) =
    callForBids(adRequest, bidder, info)

  override def prepareAd[A: Adm](bid: Bid): Task[PbAd] =
    if (Adm[A].is[Html]) {
      def missingAdmFailure = Failure(FailureReason.RtbAdmMissingFailure, "missing adm")

      val ad = for {
        jsonAdm   <- bid.customResponse.toRight(missingAdmFailure)
        stringAdm <- jsonAdm.as[String].leftMap(_ => Failure(FailureReason.AdmPrepareFailure, "could not decode amd"))
        rawMarkup <- Adm[A].parse(stringAdm)
      } yield PbAd(Adm[A].render(rawMarkup))

      Task.fromEither(ad)
    } else {
      val message = s"This adm type not supported yet"
      logger.error(message)
      Task.raiseError(new RuntimeException(message))
    }

}
