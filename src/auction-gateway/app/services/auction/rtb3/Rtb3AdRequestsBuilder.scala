package services.auction.rtb3

import java.util.UUID

import com.appodealx.exchange.common.models.Failure
import com.appodealx.exchange.common.models.FailureReason.SellerStatus
import com.appodealx.exchange.common.models.auction.Rtb3Plc
import com.appodealx.exchange.common.services.GlobalConfigService
import com.appodealx.exchange.settings.models.seller.Seller
import com.appodealx.openrtb.Device
import io.bidmachine.protobuf.adcom.{Context, Placement}
import models.RequestHost
import models.auction.AdRequest
import models.rtb3.Rtb3Request
import services.SellerFloorsSettings.SellerId
import services.auction.rtb3.DeviceWithGeoEnricher.IP
import services.{DataCenterMetadataSettings, SellerRepo}
import utils.failureutils.{liftOptionToFailureF, liftToFailureF}

import cats.MonadError
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.monadError._
import cats.syntax.option._

trait Rtb3AdRequestsBuilder[F[_]] {
  def build[P: Rtb3Plc](req: Rtb3Request, ipFromHeaders: String, host: String): F[List[AdRequest[P]]]
}

class Rtb3AdRequestsBuilderImpl[F[_]: MonadError[?[_], Throwable]](
  sellerRepo: SellerRepo[F],
  globalConfig: GlobalConfigService[F],
  dcMetadata: DataCenterMetadataSettings,
  enrichDeviceWithGeo: (Device, IP) => F[Device],
  getSellerIdFloors: SellerId => Option[List[Double]]
) extends Rtb3AdRequestsBuilder[F] {

  import models.rtb3.conversions._

  def build[P: Rtb3Plc](req: Rtb3Request, ipFromHeaders: String, host: String) =
    for {
      ad       <- liftOptionToFailureF("Invalid placement")(Rtb3Plc[P].fromRtb3(req.plc, req.ctx))
      seller   <- findSeller(req.reqExt.sellerId.toLong)
      settings <- globalConfig.read
      device   <- enrichDeviceWithGeo(req.ctx.getDevice.toRtb2Device, ipFromHeaders)
    } yield adRequest(ad, req, seller, device, host, settings.tMax)

  private def findSeller(sellerId: Long) =
    liftToFailureF("Seller not found")(sellerRepo.findSeller(sellerId))
      .ensure(Failure(SellerStatus, "is not active"))(_.active.getOrElse(false))

  private def adRequest[P: Rtb3Plc](ad: P,
                                    req: Rtb3Request,
                                    seller: Seller,
                                    device: Device,
                                    host: String,
                                    tMax: Option[Int]) = {
    val plc  = req.plc
    val ctx  = req.ctx
    val item = req.item
    val test = req.test

    def blockedList(fromPlc: Placement => List[String],
                    fromCtx: Context.Restrictions => List[String]) =
      (fromPlc(plc) ++ fromCtx(ctx.getRestrictions)).distinct.some

    seller.id
      .flatMap(getSellerIdFloors)
      .getOrElse(item.deal.toList.map(_.flr))
      .map { flr =>
        AdRequest(
          id = UUID.randomUUID.toString,
          impId = item.id.some,
          dcid = Some(dcMetadata.dcid),
          sellerBidFloor = flr,
          bidFloor = flr,
          ad = ad,
          app = ctx.getApp.toRtb2App,
          device = device,
          user = ctx.getUser.toRbt2User,
          coppa = ctx.regs.map(_.coppa),
          test = test.some,
          adUnits = Nil,
          tmax = if (test) 2000.some else tMax,
          interstitial = plc.display.exists(_.instl) || plc.video.nonEmpty,
          reward = plc.reward,
          debug = test,
          adChannel = None,
          sdk = Some(plc.sdk),
          sdkVersion = Some(plc.sdkver),
          externalCampaignImageId = None,
          metadata = true,
          sellerId = seller.id,
          sellerName = seller.name,
          adSpaceId = None,
          gdpr = ctx.regs.map(_.gdpr),
          consent = ctx.user.map(_.consent).filter(_.nonEmpty),
          host = RequestHost(host.split(":", 2)(0)),
          bcat = blockedList(fromPlc = _.bcat.toList, fromCtx = _.bcat.toList),
          badv = blockedList(fromPlc = _.badv.toList, fromCtx = _.badv.toList),
          bapp = blockedList(fromPlc = _.bapp.toList, fromCtx = _.bapp.toList),
          sellerFee = seller.fee
        )
      }
  }
}
