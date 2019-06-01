package services.auction.pb.adapters.hangmyads

import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.common.models.Platform.{iOS, Android}
import com.appodealx.exchange.common.models.Size
import com.appodealx.exchange.common.models.auction.{Bidder, Plc}
import com.appodealx.exchange.common.utils.CountryParser.parseAlpha2
import com.appodealx.openrtb.DeviceType.{Mobile, Phone, Tablet}
import com.appodealx.openrtb.Gender.{Female, Male}
import com.appodealx.openrtb.{DeviceType, Gender}
import io.circe.parser.parse
import models.auction.NoBidReason.{ParsingError, RequestException}
import models.auction.{AdRequest, AdUnit, AdapterResult, Bid, NoBidReason}
import play.api.Logger
import play.api.http.Status
import play.api.libs.ws.{WSClient, WSResponse}
import services.auction.pb.adapters.BidderCaller
import services.auction.pb.adapters.hangmyads.model.rs.{Ad, BidResponse}
import services.settings.PbSettings
import services.settings.hangmyads.HangMyAdsCpmScheme.{Current, Min}
import services.settings.hangmyads.HangMyAdsSettings

import cats.MonadError
import cats.data.EitherT
import cats.implicits._

import scala.util.Try

class HangMyAdsBidderCaller[
  F[_]: Execute: MonadError[?[_], Throwable]
](ws: WSClient, settings: HangMyAdsSettings, pbSettings: PbSettings) extends BidderCaller[F] {

  private type Params = List[(String, String)]
  private val imageBannerAdType = "1"
  private val notCached         = false
  private val logger            = Logger(this.getClass)

  override def apply[P: Plc](adRequest: AdRequest[P], bidder: Bidder, adUnits: List[AdUnit]): F[AdapterResult] =
    (for {
      params   <- makeQueryParameters(adRequest, settings)
      response <- sendRequest(bidder.endpoint.toString, params)
      bids     <- responseToBids(response, bidder.protocol.value, adUnits)
    } yield bids).value.map(notCached -> _)

  private def makeQueryParameters[P: Plc](
    adRequest: AdRequest[P],
    settings: HangMyAdsSettings
  ): EitherT[F, NoBidReason, Params] =
    Plc[P]
      .size(adRequest.ad)
      .toRight[NoBidReason](RequestException("HangMyAds: ad request has no size"))
      .map(formParametersList(adRequest, settings))
      .toEitherT[F]

  private def sendRequest(url: String, params: Params): EitherT[F, NoBidReason, WSResponse] =
    EitherT.right[NoBidReason] {

      logger.debug(s"HangMyAds request params: ${params.mkString(";")}")

      Execute[F].deferFuture {
        ws.url(url)
          .withQueryStringParameters(params: _*)
          .withRequestTimeout(pbSettings.pbTmax)
          .get
      }

    }

  private def responseToBids[P: Plc](
    res: WSResponse,
    bidderName: String,
    adUnits: List[AdUnit]
  ): EitherT[F, NoBidReason, List[Bid]] = {

    def noFill = (NoBidReason.NoFill: NoBidReason).asLeft[List[Bid]]
    def toBids = convertToBids[P](bidderName, adUnits) _

    val checkedRes =
      if (res.status == Status.OK && res.body.nonEmpty)
        res.asRight[NoBidReason]
      else
        NoBidReason.NoFill.asLeft

    logger.debug(s"HangMyAds response: ${res.body}")

    val biddingResult =
      for {
        wsRes       <- checkedRes
        json        <- parse(wsRes.body)    leftMap (e => ParsingError(e.message))
        bidResponse <- json.as[BidResponse] leftMap (e => ParsingError(e.message))
        ad          = bidResponse.ads.ad
        bids        <- ad.fold(noFill)(toBids)
      } yield bids

    logBiddingResult(biddingResult)

    biddingResult.toEitherT[F]
  }

  private def convertToBids[P: Plc](bidderName: String, adUnits: List[AdUnit])(ads: List[Ad]) = {
    def priceOf(ad: Ad) = settings.cpmScheme match {
      case Min     => Try(ad.cpm.`min_CPM`.toDouble).toOption
      case Current => Try(ad.cpm.`current_CPM`.toDouble).toOption
      case _       => None
    }

    val trackerJson = ((tracker: String) => raw"""{"imptrackers" : "$tracker"}""") andThen parse andThen(_.toOption)

    (for {
      ad    <- ads.headOption
      price <- priceOf(ad)
    } yield
      Bid(
        price = price,
        adm = buildCreative(ad.tracking.click, ad.creative.`media_file`, ad.creative.height, ad.creative.width).some,
        crid = ad.`ad_id`,
        cid = ad.campaign.id,
        apiFramework = Plc[P].apiFramework.some,
        dsp = bidderName.some,
        adUnit = adUnits.headOption,
        ext = ad.tracking.impression.flatMap(trackerJson)
      ) :: Nil).toRight(NoBidReason.NoFill: NoBidReason)
  }

  private def formParametersList(adRequest: AdRequest[_], settings: HangMyAdsSettings)(size: Size): Params = {
    val device     = adRequest.device
    val deviceType = device.devicetype.map(toHangMyAdsDevType)
    val ip         = device.ip.orElse(device.ipv6)
    val geo        = device.geo
    val country    = geo.flatMap(_.country).flatMap(parseAlpha2)
    val city       = geo.flatMap(_.city).map(_.toLowerCase)
    val gender     = adRequest.user.gender.map(toHangMyAdsGender)

    val adIdName = device.os.flatMap { s =>
      s.toLowerCase match {
        case Android.entryName => Some("gaid")
        case iOS.entryName     => Some("idfa")
        case _                 => None
      }
    }

    List(
      "pub"                     -> settings.internalId.toString,
      "h"                       -> size.height.toString,
      "w"                       -> size.width.toString,
      "res"                     -> "JSON",
      "ad_type"                 -> imageBannerAdType,
      "lim"                     -> "1"
    ) ++ device.os.map("os"     -> _) ++
      deviceType.map("dev_type" -> _) ++
      ip.map("ip"               -> _) ++
      country.map("country"     -> _) ++
      city.map("city"           -> _) ++
      gender.map("gender"       -> _) ++
      (adIdName                 -> device.ifa).bisequence

  }

  private def buildCreative(click: String, media: String, h: String, w: String) =
    s"<a href='$click' target='_top'><img src='$media' height='$h' width='$w' border='0'/></a>"

  private def toHangMyAdsDevType(devType: DeviceType) =
    devType match {
      case Phone  => "phone"
      case Tablet => "tablet"
      case Mobile => "both"
      case _      => ""
    }

  private def toHangMyAdsGender(gender: Gender) =
    gender match {
      case Male   => "male"
      case Female => "female"
      case _      => "other"
    }

  private def logBiddingResult(biddingResult: Either[NoBidReason, List[Bid]]) =
    logger.debug {
      if (biddingResult.isRight)
        "HangMyAds: successfully acquired bids"
      else
        s"HangMyAds: ${biddingResult.left.get.prettyValue}"
    }
}
