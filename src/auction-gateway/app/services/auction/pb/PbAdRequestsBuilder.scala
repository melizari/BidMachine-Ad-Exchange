package services.auction.pb

import java.util.UUID

import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.exchange.common.models.{Failure, FailureReason}
import com.appodealx.exchange.settings.persistance.seller.repos.AdSpaceRepo
import com.appodealx.openrtb
import models.auction.{AdRequest, AdUnit}
import models.{RequestHost, SdkRequest}
import services.auction.Auction
import services.{AppRepo, DataCenterMetadataSettings, SellerRepo}
import utils.failureutils._

import cats.MonadError

trait PbAdRequestsBuilder[F[_]] {
  def buildRequests[P: Plc](dto: SdkRequest,
                            info: List[AdUnit],
                            host: RequestHost,
                            tmax: Option[Int]): F[List[AdRequest[P]]]
}

class PbAdRequestsBuilderImpl[F[_]](sellerRepo: SellerRepo[F],
                                    appRepo: AppRepo[F],
                                    adSpaceRepo: AdSpaceRepo[F],
                                    dcMetadata: DataCenterMetadataSettings)(
  implicit M: MonadError[F, Throwable]
) extends PbAdRequestsBuilder[F] {

  def buildRequests[P: Plc](
    dto: SdkRequest,
    info: List[AdUnit],
    host: RequestHost,
    tmax: Option[Int]
  ): F[List[AdRequest[P]]] = {

    import models.auction.RtbAppExtOps

    import cats.syntax.applicative._
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    import cats.syntax.monadError._

    def mergeExt(app: openrtb.App) = {
      val ext = (app.ext ++ dto.`app_ext`).reduceOption(_.deepMerge(_))
      app.copy(ver = dto.ver, ext = ext)
    }

    val appIdEff = liftToFailureF("App id missing")(dto.`external_app_id`.pure[F])

    val sellerEff = liftToFailureF("Seller not found")(sellerRepo.findSeller(dto.`publisher_id`))
      .ensure(Failure(FailureReason.SellerStatus, "is not active"))(_.active.getOrElse(false))

    val adSpace = adSpaceRepo.find[P](dto.`ad_space_id`)
    val adSpaceEff = liftToFailureF("AdSpace not found")(adSpace)
      .ensure(Failure(FailureReason.AdSpaceStatus, "is not active"))(_.active)

    val appEff = for {
      appId  <- appIdEff
      seller <- sellerEff
      app    <- liftToFailureF("App not found")(appRepo.findAppInKs(seller.ks.getOrElse("internal"), appId)).map(mergeExt)
    } yield app

    for {
      app     <- appEff
      adSpace <- adSpaceEff
      seller  <- sellerEff
    } yield {

      val (session, impression) = app.ext.map(_.sessionInfo[P](adSpace)).getOrElse((None, None))

      dto.bidfloors.map { bidfloor =>
        AdRequest[P](
          id = UUID.randomUUID.toString,
          impId = dto.impid,
          dcid = Some(dcMetadata.dcid),
          sellerBidFloor = bidfloor,
          bidFloor = bidfloor,
          ad = adSpace.ad,
          adSpaceId = adSpace.id,
          app = app,
          device = dto.toRtbDevice,
          user = dto.toRtbUser,
          coppa = dto.coppa.map(_ > 0),
          test = dto.test.map(_ > 0),
          adUnits = info,
          tmax = tmax,
          // For bidder auction
          interstitial = adSpace.interstitial,
          reward = adSpace.reward,
          debug = adSpace.debug,
          adChannel = adSpace.adChannel,
          sdk = adSpace.displayManager,
          sdkVersion = dto.`dm_ver`,
          externalCampaignImageId = dto.`e_ci_id`,
          metadata = dto.`metadata_headers`.exists(_ > 0),
          sellerId = seller.id,
          sellerName = seller.name,
          sellerFee = seller.fee,
          gdpr = dto.gdpr.map(_ > 0),
          consent = dto.consent,
          sesN = session,
          impN = impression,
          host = host
        )
      }
    }
  }
}
