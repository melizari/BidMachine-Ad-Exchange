package services.auction.rtb3

import com.appodealx.exchange.common.models.auction.{Adm, Plc, Rtb3Plc}
import com.appodealx.exchange.common.models.dto.{Banner, Native, Video}
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.openrtb.native.response.{Native => ResNative}
import io.bidmachine.protobuf.adcom
import io.bidmachine.protobuf.openrtb.Openrtb
import models.Ad
import models.rtb3.{Rtb3Request, Rtb3RequestInfo}
import play.twirl.api.Html
import services.auction.{Auction, AuctionProxy}
import services.settings.SellerAuctionsSettings

import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.parallel._
import cats.{Monad, Parallel}

import scala.util.Random.shuffle

class Rtb3Service[F[_]: Monad: Parallel[?[_], F]](unpacker: Rtb3Unpacker[F],
                                                  proxy: AuctionProxy[F],
                                                  sellerAuctionsSettings: SellerAuctionsSettings[F],
                                                  requestBuilder: Rtb3AdRequestsBuilder[F],
                                                  resMapper: Rtb3BidResponseMapper) {

  def performAuction(rtb: Openrtb, ipFromHeaders: String, host: String): F[Option[Openrtb]] =
    for {
      req     <- unpacker.makeRtb3ReqOf(rtb)
      ad      <- requestAd(req, ipFromHeaders, host)
      reqInfo = Rtb3RequestInfo.of(req)
    } yield ad flatMap resMapper.mapToRtb3Response(reqInfo)

  private def requestAd(req: Rtb3Request, ipFromHeaders: String, host: String) = {

    def adOf[A: Adm, P: Plc: Rtb3Plc](req: Rtb3Request): F[Option[Ad]] =
      for {
        adReqs <- requestBuilder.build[P](req, ipFromHeaders, host)
        sellerId  = req.reqExt.sellerId.toLong
        auctions  = sellerAuctionsSettings.getAuctionsBySellerId(sellerId).getOrElse(sellerAuctionsSettings.defaultAuctions)
        ad     <- proxy.perform[A, P](adReqs, auctions)
      } yield ad

    def winAdOf(ads: Option[Ad]*) = shuffle(ads.toList.flatten).sortBy(_.sspIncome)(Ordering[Double].reverse).headOption

    req.plc match {

      case isBannerAndVideoRequest() =>
        (adOf[Html, Banner](req), adOf[VAST, Video](req)).parMapN {
          case (bannerAd, videoAd) => winAdOf(bannerAd, videoAd)
        }

      case isBannerRequest() => adOf[Html, Banner](req)
      case isVideoRequest()  => adOf[VAST, Video](req)
      case isNativeRequest() => adOf[ResNative, Native](req)
      case _                 => none[Ad].pure[F]
    }
  }
  private object isBannerRequest {
    def unapply(p: adcom.Placement): Boolean = p.display.exists(d => d.nativefmt.isEmpty)
  }

  private object isNativeRequest {
    def unapply(p: adcom.Placement) = p.display.exists(d => d.nativefmt.nonEmpty)
  }

  private object isVideoRequest {
    def unapply(p: adcom.Placement) = p.video.nonEmpty
  }

  private object isBannerAndVideoRequest {
    def unapply(p: adcom.Placement): Boolean = (p.display, p.video) match {
      case (Some(display), Some(_)) => display.instl
      case _                        => false
    }
  }
}
