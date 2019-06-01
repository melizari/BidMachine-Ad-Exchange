package models

import java.util.UUID

import com.appodealx.exchange.common.models.analytics.AdType
import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.exchange.common.models.dto.{Banner, Native, Video}
import com.appodealx.exchange.common.models.{Platform, Size}
import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.exchange.settings.models.seller.AdSpace
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo
import com.appodealx.exchange.settings.persistance.buyer.repos.BidderRepo.Match
import com.appodealx.openrtb.AuctionType.SecondPrice
import com.appodealx.openrtb.{BidRequest, BidResponse, Imp, Regs}
import com.github.zafarkhaja.semver.Version
import io.circe.Json
import io.circe.syntax._
import models.circe.CirceAuctionSettingsInstances

import scala.util.Try

package object auction {

  type BiddingResult = Either[NoBidReason, List[Bid]]

  type Cached     = Boolean
  type ClearedBid = (Double, Bid)

  type AdapterResult = (Cached, BiddingResult)

  type AuctionMeta        = (Match, Cached)
  type AuctionItem[A]     = (AuctionMeta, A)
  type AuctionItemList[A] = List[AuctionItem[A]]
  type AuctionResult      = (AuctionItemList[NoBidReason], AuctionItemList[ClearedBid])

  implicit class ImpOps(imp: Imp) {
    def inject[P: Plc](repr: P) = Plc[P].toRtb2(repr, imp)
  }

  implicit class CpmOps(price: Double) {
    def roundCPM: Double = (math rint price * 10000) / 10000
  }

  implicit class PimpedAdRequest[T: Plc](adRequest: AdRequest[T]) {
    private val P    = Plc[T]
    private val size = P.size(adRequest.ad)

    def adType: AdType = P match {
      case plc if plc.is[Banner] && adRequest.interstitial      => AdType.Interstitial
      case plc if plc.is[Banner] && size.contains(Size.Mrec)    => AdType.Mrec
      case plc if plc.is[Banner]                                => AdType.Banner
      case plc if plc.is[Video] && adRequest.reward             => AdType.NonSkippableVideo
      case plc if plc.is[Video]                                 => AdType.Video
      case plc if plc.is[Native]                                => AdType.Native
    }

    def prettyAdType: String = {
      val instl    = if (adRequest.interstitial) "interstitial" else "noninterstitial"
      val rewarded = if (adRequest.reward) "rewarded" else "nonrewarded"
      s"${rewarded}_${instl}_${ Plc[T].name}"
    }

    def sizeString: String = size.map(s => s"${s.width}x${s.height}").getOrElse("0x0")

    def bidderRepoQuery =
      BidderRepo.Query(
        ad = adRequest.ad,
        debug = adRequest.debug,
        adChannel = adRequest.adChannel,
        interstitial = adRequest.interstitial,
        reward = adRequest.reward,
        coppa = adRequest.coppa,
        countries = adRequest.countries,
        platforms = adRequest.platforms,
        dmVer = adRequest.sdkVersion.flatMap(ver => Try(Version.valueOf(ver)).toOption),
        sellerId = adRequest.sellerId
      )

    def countries = {
      val deviceGeoCountry = adRequest.device.geo.flatMap(_.country)
      val userGeoCountry   = adRequest.user.geo.flatMap(_.country)

      deviceGeoCountry.orElse(userGeoCountry).map(Set(_).toList)
    }

    def platforms =
      adRequest.device.os.flatMap(Platform.fromString).map(List(_))

    def bidRequest: BidRequest = {
      import com.appodealx.exchange.common.utils.analytics._

      val impId          = UUID.randomUUID.toString

      val imp = Imp(
        id = impId,
        bidfloor = Some(adRequest.bidFloor),
        instl = Some(adRequest.interstitial),
        displaymanager = adRequest.sdk,
        displaymanagerver = adRequest.sdkVersion
      ).inject(adRequest.ad)

      // Parsing country to ISO 3166-1 alpha-2/3 format
      // For bidder request need `ISO 3166-1 alpha-3` format!
      // For stats in appodeal need `ISO 3166-1 alpha-2` format!
      val rtbDevice = {
        val geo = adRequest.device.geo.map { g =>
          g.copy(country = g.country.flatMap(CountryParser.parseAlpha3))
        }

        adRequest.device.copy(geo = geo, js = Some(true))
      }

      BidRequest(
        id = adRequest.id,
        imp = List(imp),
        app = Some(adRequest.app),
        device = Some(rtbDevice),
        user = Some(adRequest.user),
        test = adRequest.test,
        at = Some(SecondPrice),
        bcat = adRequest.app.ext.flatMap(parseBcat).orElse(adRequest.bcat),
        badv = adRequest.app.ext.flatMap(parseBadv).orElse(adRequest.badv),
        tmax = adRequest.tmax,
        regs = Some(
          Regs(
            coppa = adRequest.coppa,
            ext = adRequest.gdpr.map(b => Json.obj("gdpr" := (if (b) 1 else 0)))
          )
        )
      )
    }

  }

  implicit class PimpedBidResponse(response: BidResponse) {
    def toBids: List[Bid] =
      response.seatbid.toList.flatten.flatMap(
        seatBid =>
          seatBid.bid.map(
            bid =>
              Bid(
                price = bid.price,
                adm = bid.adm,
                adid = bid.adid,
                impid = Some(bid.impid),
                adomain = bid.adomain,
                bundle = bid.bundle,
                cat = bid.cat,
                attr = bid.attr,
                iurl = bid.iurl,
                cid = bid.cid,
                crid = bid.crid,
                qagmediarating = bid.qagmediarating,
                customResponse = None,
                bidId = response.bidid,
                seatId = seatBid.seat,
                nurl = bid.nurl,
                burl = bid.burl,
                lurl = bid.lurl,
                ext = bid.ext
            )
        )
      )
  }

  implicit class RtbAppExtOps(ext: Json) extends CirceAuctionSettingsInstances {

    def sessionInfo[T: Plc](adSpace: AdSpace[T]): (Option[Long], Option[Long]) = {

      val P    = Plc[T]
      val ad   = adSpace.ad
      val size = P.size(ad)

      def impN(m: SessionMetrics) = P match {
        case p if p.is[Banner] && adSpace.interstitial     => m.interstitial
        case p if p.is[Banner] && size.contains(Size.Mrec) => m.mrec
        case p if p.is[Banner]                             => m.banner
        case p if p.is[Video] && adSpace.reward            => m.`rewarded_video`
        case p if p.is[Video]                              => m.video
        case p if p.is[Native]                             => m.native
      }

      val appExt         = ext.as[SessionExtension].toOption
      val sessionNumber  = appExt.flatMap(_.`session_id`)
      val sessionMetrics = appExt.flatMap(_.imp)

      val impressionNumber = Some(sessionMetrics.flatMap(impN).fold(1L)(_ + 1))

      (sessionNumber, impressionNumber)
    }
  }
}
