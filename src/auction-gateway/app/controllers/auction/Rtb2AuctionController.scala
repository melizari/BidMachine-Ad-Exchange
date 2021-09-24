package controllers.auction

import java.util.UUID

import com.appodealx.exchange.common.models.auction._
import com.appodealx.exchange.common.models.dto.{Banner, Native, Video}
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.exchange.common.models.{auction => _, _}
import com.appodealx.exchange.common.services.GlobalConfigService
import com.appodealx.exchange.settings.models.seller.AdSpaceId
import com.appodealx.exchange.settings.persistance.seller.repos.AdSpaceRepo
import com.appodealx.openrtb.BidRequest
import com.appodealx.openrtb.native.response
import controllers.actions.Actions
import controllers.auction.renderers.RtbAdMarkupRendering
import kamon.AdRequestMetrics
import models._
import models.auction.AdRequest
import models.circe.CirceAuctionSettingsInstances
import monix.eval.Task
import monix.execution.Scheduler
import play.api.libs.circe.Circe
import play.api.mvc._
import play.twirl.api.Html
import services.auction.{Auction, AuctionProxy}
import services.settings.SellerAuctionsSettings
import services.{DataCenterMetadataSettings, SellerRepo}
import utils.failureutils._

import scala.concurrent.Future

class Rtb2AuctionController(
                             adSpaceRepo: AdSpaceRepo[Task],
                             sellerRepo: SellerRepo[Task],
                             cc: ControllerComponents,
                             auction: AuctionProxy[Task],
                             sellerAuctionsSettings: SellerAuctionsSettings[Task],
                             dcMetadata: DataCenterMetadataSettings
)(
  implicit val scheduler: Scheduler,
  val globalConfig: GlobalConfigService[Task]
) extends AbstractController(cc)
    with RtbAdMarkupRendering
    with Actions
    with Circe
    with CirceAuctionSettingsInstances {

  def action = NoFillAction.async(circe.json[BidRequest]) { implicit request =>
    val bidRequest = request.body

    bidRequest.imp.headOption match {
      case Some(imp) if imp.banner.isDefined => performAuction[Html, Banner](bidRequest)
      case Some(imp) if imp.video.isDefined  => performAuction[VAST, Video](bidRequest)
      case Some(imp) if imp.native.isDefined => performAuction[response.Native, Native](bidRequest)
      case _                                 => Future(NoContent)
    }
  }

  private def performAuction[A: Adm, P: Plc](bidRequest: BidRequest)(implicit r: RequestHeader) = {
    for {
      req      <- buildRequest[P](bidRequest)
      _        <- measureMetrics(req)
      auctions = getAuctions(req)
      adm      <- auction.perform[A, P](req :: Nil, auctions)
    } yield adm.filterNot(isBlocked(req)).fold(NoContent)(renderAd[P](bidRequest))
  }.onErrorRecover {
    case f: Failure =>
      NoContent.withHeaders(
        "ad-exchange-error-message" -> f.message,
        "ad-exchange-error-reason"  -> f.reason.entryName
      )
    case e: Exception => NoContent.withHeaders("ad-exchange-error-message" -> e.getMessage)
  }.runToFuture

  private def getAuctions(req: AdRequest[_]) = {
    req.sellerId.flatMap(sellerAuctionsSettings.getAuctionsBySellerId).getOrElse(sellerAuctionsSettings.defaultAuctions)
  }

  private def isBlocked(request: AdRequest[_])(ad: Ad) = {
    val badvSet = request.badv.toSet.flatten
    val bcatSet = request.bcat.toSet.flatten
    val bappSet = request.bapp.toSet.flatten

    val bundle  = ad.bundle
    val adomain = ad.adomain.toSet.flatten
    val cat     = ad.cat.toSet.flatten

    (bundle exists bappSet) || (cat exists bcatSet) || (adomain exists badvSet)
  }

  private def measureMetrics[P: Plc](req: AdRequest[P]) =
    AdRequestMetrics
      .measureMetrics[Task, P](req.device.geo.flatMap(_.country),
                               req.device.os,
                               req.adSpaceId.map(_.value),
                               req.sellerId,
                               req.interstitial,
                               pb = false)
      .onErrorRecover { case _ => () }

  private def buildRequest[T: Plc](bidRequest: BidRequest)(implicit r: RequestHeader) = {

    import cats.syntax.monadError._

    def sellerEff(id: Long) =
      liftTask("Seller not found")(sellerRepo.findSeller(id))
        .ensure(Failure(FailureReason.SellerStatus, "is not active"))(_.active.getOrElse(false))

    def adSpaceEff(id: Long) =
      liftTask("AdSpace not found")(adSpaceRepo.find[T](AdSpaceId(id)))
        .ensure(Failure(FailureReason.AdSpaceStatus, "is not active"))(_.active)

    val globalSettingsEff = globalConfig.read

    val extEff = liftTask("Appodeal Extension not found")(
      Task.pure(bidRequest.ext.flatMap(ext => ext.as[BidRequestExtension].toOption))
    )

    val adRequestOpt = for {
      ext     <- extEff
      seller  <- sellerEff(ext.ssp_id)
      adSpace <- adSpaceEff(ext.ad_space_id)
      gsp     <- globalSettingsEff
    } yield {
      for {
        app      <- bidRequest.app
        device   <- bidRequest.device
        user     <- bidRequest.user
        imp      <- bidRequest.imp.headOption
        bidFloor <- imp.bidfloor
      } yield
        AdRequest[T](
          id = UUID.randomUUID.toString,
          dcid = Some(dcMetadata.dcid),
          sellerBidFloor = bidFloor,
          bidFloor = bidFloor,
          ad = adSpace.ad,
          interstitial = adSpace.interstitial,
          reward = adSpace.reward,
          debug = adSpace.debug,
          coppa = bidRequest.regs.flatMap(_.coppa),
          app = app,
          device = device,
          user = user,
          test = bidRequest.test,
          gdpr = bidRequest.regs.flatMap(_.ext.flatMap(_.hcursor.downField("gdpr").as[Int].toOption)).map(_ > 0),
          adChannel = adSpace.adChannel,
          sdk = imp.displaymanager,
          sdkVersion = imp.displaymanagerver,
          externalCampaignImageId = None,
          tmax = gsp.tMax,
          metadata = ext.metadata_headers.exists(_ > 0),
          sellerId = seller.id,
          sellerName = seller.name,
          at = bidRequest.at,
          bcat = Some(bidRequest.bcat.getOrElse(Nil) ++ seller.bcat.getOrElse(Nil)),
          badv = Some(bidRequest.badv.getOrElse(Nil) ++ seller.badv.getOrElse(Nil)),
          bapp = Some(bidRequest.bapp.getOrElse(Nil) ++ seller.bapp.getOrElse(Nil)),
          adSpaceId = adSpace.id,
          host = RequestHost(r.host.split(":", 2)(0)),
          adUnits = Nil,
          impId = None,
          sellerFee = seller.fee
        )
    }

    liftTask("Missing device, user or impression data")(adRequestOpt)
  }
}
