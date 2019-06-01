package services.auction.pb

import com.appodealx.exchange.common.models.PbMarkup
import com.appodealx.exchange.common.models.auction.Adm
import io.circe.{Json, Printer}
import models.PbAd
import models.auction.{Bid, BiddingResult, NoBidReason}

import cats.Applicative

abstract class DefaultAdapter[F[_]: Applicative](val name: String) extends Adapter[F] {
  protected val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  protected def flatResults(results: List[BiddingResult]): BiddingResult =
    results.foldLeft[BiddingResult](Left(NoBidReason.NoFill)) {
      case (Right(b1), Right(b2))    => Right(b1 ++ b2)
      case (res @ Right(_), Left(_)) => res
      case (Left(_), res @ Right(_)) => res
      case (Left(_), nbr @ Left(_))  => nbr
    }

  def prepareAd[A: Adm](bid: Bid): F[PbAd] =
    Applicative[F].pure {
      val emptyJson = Json.obj()
      val reqJson   = bid.adUnit.flatMap(_.customParams).getOrElse(emptyJson)
      val bidJson   = bid.customResponse.getOrElse(emptyJson)

      val markup = PbMarkup(reqJson deepMerge bidJson)

      PbAd(markup)
    }
}
