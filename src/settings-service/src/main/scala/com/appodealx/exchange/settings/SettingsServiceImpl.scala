package com.appodealx.exchange.settings

import akka.{Done, NotUsed}
import com.appodealx.exchange.common.models.auction._
import com.appodealx.exchange.settings.models.buyer._
import com.appodealx.exchange.settings.models.seller.{AdSpaceId, BannerAdSpace, NativeAdSpace, VideoAdSpace}
import com.appodealx.exchange.settings.persistance.buyer.dao._
import com.appodealx.exchange.settings.persistance.seller.dao._
import com.appodealx.exchange.settings.persistance.seller.repos._
import com.appodealx.exchange.settings.services.{AgencyExternalService, AgencySyncService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{BadRequest, NotFound}
import monix.eval.Task
import monix.execution.Scheduler
import org.postgresql.util.PSQLException
import play.api.mvc.{Result, Results}

import cats.data.OptionT

import scala.concurrent.Future

class SettingsServiceImpl(syncService: AgencySyncService,
                          externalAgencyService: AgencyExternalService,
                          agencyDAO: AgencyDAO,
                          bidderDAO: BidderDAO,
                          bannerAdProfileDAO: BannerAdProfileDAO,
                          videoAdProfileDAO: VideoAdProfileDAO,
                          nativeAdProfileDAO: NativeAdProfileDAO,
                          sellers: SellerRepository,
                          bannerAdSpaceDAO: BannerAdSpaceDAO,
                          videoAdSpaceDAO: VideoAdSpaceDAO,
                          nativeAdSpaceDAO: NativeAdSpaceDAO)(implicit scheduler: Scheduler)
    extends SettingsService {

  override def readAllAgencies: ServiceCall[NotUsed, List[Agency]] = ServiceCall { _ =>
    agencyDAO.findAll.map(_.toList)
  }

  override def readAgency(id: Long): ServiceCall[NotUsed, Agency] = ServiceCall { _ =>
    agencyDAO.find(AgencyId(id)).map(_.getOrElse(throw NotFound("Agency not found!"))).runToFuture
  }

  override def createAgency: ServiceCall[Agency, Agency] = ServiceCall { agency =>
    agencyDAO.insert(agency).runToFuture
  }

  override def updateAgency(id: Long): ServiceCall[Agency, Agency] = ServiceCall { request =>
    OptionT(agencyDAO.find(AgencyId(id))).flatMapF { agency =>
      val updatedAgency = request.copy(
        id = Some(AgencyId(id)),
        externalId = agency.externalId,
        active = agency.active
      )

      val needsSync = updatedAgency.externalId.isDefined

      if (needsSync) {
        syncService.updateAgency(updatedAgency).flatMap { updated =>
          if (updated) {
            agencyDAO.update(updatedAgency)
          } else {
            Task.pure(None)
          }
        }
      } else {
        agencyDAO.update(updatedAgency)
      }
    }.value.map(_.getOrElse(throw NotFound("Agency not updated!"))).runToFuture
  }

  override def deleteAgency(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    agencyDAO.delete(AgencyId(id)).map(if (_) Done else throw NotFound("Agency not found!")).runToFuture
  }

  override def activateAgency(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    updateActiveStatusAgency(id, newActiveStatus = true).map(_ => Done).runToFuture
  }

  override def deactivateAgency(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    updateActiveStatusAgency(id, newActiveStatus = false).map(_ => Done).runToFuture
  }

  override def readBiddersByAgencyId(agencyId: Long): ServiceCall[NotUsed, List[Bidder]] = ServiceCall { _ =>
    bidderDAO.findByAgencyId(AgencyId(agencyId)).map(_.toList)
  }

  override def readAllBidders: ServiceCall[NotUsed, List[Bidder]] = ServiceCall { _ =>
    bidderDAO.findAll.map(_.toList)
  }

  override def readBidder(id: Long): ServiceCall[NotUsed, Bidder] = ServiceCall { _ =>
    bidderDAO.find(BidderId(id)).map(_.getOrElse(throw NotFound("Bidder not found!")))
  }

  override def createBidder: ServiceCall[Bidder, Bidder] = ServiceCall { bidder =>
    if (bothTargetingFields(bidder))
      Future.successful(
        throw BadRequest("Fields \"ExcludedSellers\" and \"IncludedSellers\" are mutually exclusive parameters")
      )
    else bidderDAO.insert(bidder)
  }

  override def updateBidder(id: Long): ServiceCall[Bidder, Bidder] = ServiceCall { bidder =>
    if (bothTargetingFields(bidder))
      Future.successful(
        throw BadRequest("Fields \"ExcludedSellers\" and \"IncludedSellers\" are mutually exclusive parameters")
      )
    else bidderDAO.update(bidder.copy(id = Some(BidderId(id)))).map(_.fold(throw NotFound("Bidder not found!"))(b => b))
  }

  override def deleteBidder(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bidderDAO.delete(BidderId(id)).map(if (_) Done else throw NotFound("Bidder not found!"))
  }

  override def readBannerAdProfiles(id: Long): ServiceCall[NotUsed, List[BannerAdProfile]] = ServiceCall { _ =>
    bannerAdProfileDAO.findByBidderId(BidderId(id)).runToFuture.map(_.toList)
  }

  override def readVideoAdProfiles(id: Long): ServiceCall[NotUsed, List[VideoAdProfile]] = ServiceCall { _ =>
    videoAdProfileDAO.findByBidderId(BidderId(id)).runToFuture.map(_.toList)
  }

  override def readNativeAdProfiles(id: Long): ServiceCall[NotUsed, List[NativeAdProfile]] = ServiceCall { _ =>
    nativeAdProfileDAO.findByBidderId(BidderId(id)).runToFuture.map(_.toList)
  }

  override def readAllBannerAdProfiles: ServiceCall[NotUsed, List[BannerAdProfileWithBidder]] = ServiceCall { _ =>
    bannerAdProfileDAO.findAll
      .map(
        seq =>
          seq.map { t =>
            val (profile, bidder) = t
            BannerAdProfileWithBidder(
              profile.id,
              profile.bidderId,
              profile.title,
              profile.active,
              profile.debug,
              profile.adChannel,
              profile.delayedNotification,
              profile.interstitial,
              profile.reward,
              profile.ad,
              profile.dmVerMax,
              profile.dmVerMin,
              profile.distributionChannel,
              profile.template,
              bidder,
              profile.allowCache,
              profile.allowCloseDelay
            )
        }
      )
      .map(_.toList)
      .runToFuture
  }

  override def readBannerAdProfile(id: Long): ServiceCall[NotUsed, BannerAdProfile] = ServiceCall { _ =>
    bannerAdProfileDAO
      .find(AdProfileId(id))
      .map(_.fold(throw NotFound("BannerAdProfile not found!"))(p => p))
      .runToFuture
  }

  override def createBannerAdProfile: ServiceCall[BannerAdProfile, BannerAdProfile] = ServiceCall { bannerAdProfile =>
    bannerAdProfileDAO.insert(bannerAdProfile).runToFuture
  }

  override def updateBannerAdProfile(id: Long): ServiceCall[BannerAdProfile, BannerAdProfile] = ServiceCall {
    bannerAdProfile =>
      bannerAdProfileDAO
        .update(bannerAdProfile.copy(id = Some(AdProfileId(id))))
        .map(_.fold(throw NotFound("BannerAdProfile not found!"))(p => p))
        .runToFuture
  }

  override def deleteBannerAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bannerAdProfileDAO
      .delete(AdProfileId(id))
      .map(if (_) Done else throw NotFound("BannerAdProfile not found!"))
      .runToFuture
  }

  override def activateBannerAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bannerAdProfileDAO.updateActive(AdProfileId(id), active = true).map(_ => Done).runToFuture
  }

  override def deactivateBannerAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bannerAdProfileDAO.updateActive(AdProfileId(id), active = false).map(_ => Done).runToFuture
  }

  override def readAllVideoAdProfiles: ServiceCall[NotUsed, List[VideoAdProfileWithBidder]] = ServiceCall { _ =>
    videoAdProfileDAO.findAll
      .map(
        seq =>
          seq.map { t =>
            val (profile, bidder) = t
            VideoAdProfileWithBidder(
              profile.id,
              profile.bidderId,
              profile.title,
              profile.active,
              profile.debug,
              profile.adChannel,
              profile.delayedNotification,
              profile.interstitial,
              profile.reward,
              profile.ad,
              profile.dmVerMax,
              profile.dmVerMin,
              profile.distributionChannel,
              profile.template,
              bidder,
              profile.allowCache,
              profile.allowCloseDelay
            )
        }
      )
      .map(_.toList)
      .runToFuture
  }

  override def readVideoAdProfile(id: Long): ServiceCall[NotUsed, VideoAdProfile] = ServiceCall { _ =>
    videoAdProfileDAO
      .find(AdProfileId(id))
      .map(_.fold(throw NotFound("VideoAdProfile not found!"))(p => p))
      .runToFuture
  }

  override def createVideoAdProfile: ServiceCall[VideoAdProfile, VideoAdProfile] = ServiceCall { videoAdProfile =>
    videoAdProfileDAO.insert(videoAdProfile).runToFuture
  }

  override def updateVideoAdProfile(id: Long): ServiceCall[VideoAdProfile, VideoAdProfile] = ServiceCall {
    videoAdProfile =>
      videoAdProfileDAO
        .update(videoAdProfile.copy(id = Some(AdProfileId(id))))
        .map(_.fold(throw NotFound("VideoAdProfile not found!"))(p => p))
        .runToFuture
  }

  override def deleteVideoAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    videoAdProfileDAO
      .delete(AdProfileId(id))
      .map(if (_) Done else throw NotFound("VideoAdProfile not found!"))
      .runToFuture
  }

  override def activateVideoAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    videoAdProfileDAO.updateActive(AdProfileId(id), active = true).map(_ => Done).runToFuture
  }

  override def deactivateVideoAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    videoAdProfileDAO.updateActive(AdProfileId(id), active = false).map(_ => Done).runToFuture
  }

  override def readAllNativeAdProfiles: ServiceCall[NotUsed, List[NativeAdProfileWithBidder]] = ServiceCall { _ =>
    nativeAdProfileDAO.findAll
      .map(
        seq =>
          seq.map { t =>
            val (profile, bidder) = t
            NativeAdProfileWithBidder(
              profile.id,
              profile.bidderId,
              profile.title,
              profile.active,
              profile.debug,
              profile.adChannel,
              profile.delayedNotification,
              profile.interstitial,
              profile.reward,
              profile.ad,
              profile.dmVerMax,
              profile.dmVerMin,
              profile.distributionChannel,
              profile.template,
              bidder,
              profile.allowCache,
              profile.allowCloseDelay
            )
        }
      )
      .map(_.toList)
      .runToFuture
  }

  override def readNativeAdProfile(id: Long): ServiceCall[NotUsed, NativeAdProfile] = ServiceCall { _ =>
    nativeAdProfileDAO
      .find(AdProfileId(id))
      .map(_.fold(throw NotFound("NativeAdProfile not found!"))(p => p))
      .runToFuture
  }

  override def createNativeAdProfile: ServiceCall[NativeAdProfile, NativeAdProfile] = ServiceCall { nativeAdProfile =>
    nativeAdProfileDAO.insert(nativeAdProfile).runToFuture
  }

  override def updateNativeAdProfile(id: Long): ServiceCall[NativeAdProfile, NativeAdProfile] = ServiceCall {
    nativeAdProfile =>
      nativeAdProfileDAO
        .update(nativeAdProfile.copy(id = Some(AdProfileId(id))))
        .map(_.fold(throw NotFound("NativeAdProfile not found!"))(p => p))
        .runToFuture
  }

  override def deleteNativeAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    nativeAdProfileDAO
      .delete(AdProfileId(id))
      .map(if (_) Done else throw NotFound("NativeAdProfile not found!"))
      .runToFuture
  }

  override def activateNativeAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    nativeAdProfileDAO.updateActive(AdProfileId(id), active = true).map(_ => Done).runToFuture
  }

  override def deactivateNativeAdProfile(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    nativeAdProfileDAO.updateActive(AdProfileId(id), active = false).map(_ => Done).runToFuture
  }

  def findAllSellers = ServiceCall { _ =>
    sellers.findAll.map(_.toList)
  }

  def findSeller(id: Long) = ServiceCall { _ =>
    sellers.findOne(id).map(_.getOrElse(throw NotFound("Seller not found.")))
  }

  def insertSeller = ServiceCall(sellers.insert)

  def updateSeller(id: Long) = ServiceCall { seller =>
    sellers.update(seller.copy(id = Some(id))).map(_.fold(throw NotFound("Seller not found!"))(s => s)).recover {
      case e: PSQLException if e.getSQLState == "23505" => throw BadRequest("Seller with such ks already exists")
    }
  }

  def deleteSeller(id: Long) = ServiceCall(_ => sellers.delete(id).map(_ => Done))

  def status = ServiceCall { _ =>
    Future.successful(Done)
  }

  override def createBannerAdSpaceWithSellerId(sellerId: Long): ServiceCall[BannerAdSpace, BannerAdSpace] =
    ServiceCall { adSpace =>
      bannerAdSpaceDAO.insertWithSellerId(sellerId, adSpace)
    }

  override def readBannerBySellerId(sellerId: Long): ServiceCall[NotUsed, List[BannerAdSpace]] = ServiceCall { _ =>
    bannerAdSpaceDAO.findBySellerId(sellerId).map(_.toList)
  }

  override def readBannerAdSpaceById(id: Long): ServiceCall[NotUsed, Option[BannerAdSpace]] = ServiceCall { _ =>
    bannerAdSpaceDAO.findById(AdSpaceId(id))
  }

  override def updateBannerAdSpace(id: Long): ServiceCall[BannerAdSpace, BannerAdSpace] = ServiceCall { adSpace =>
    bannerAdSpaceDAO
      .update(adSpace.copy(id = Some(AdSpaceId(id))))
      .map(_.fold(throw NotFound(""))(b => b))
  }

  override def activateBannerAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bannerAdSpaceDAO
      .updateActive(AdSpaceId(id), active = true)
      .map(if (_) Done else throw NotFound(""))
  }

  override def deactivateBannerAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bannerAdSpaceDAO
      .updateActive(AdSpaceId(id), active = false)
      .map(if (_) Done else throw NotFound(""))
  }

  override def deleteBannerAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bannerAdSpaceDAO
      .delete(AdSpaceId(id))
      .map(if (_) Done else throw NotFound(""))
  }

  override def createVideoAdSpaceWithSellerId(sellerId: Long): ServiceCall[VideoAdSpace, VideoAdSpace] = ServiceCall {
    adSpace =>
      videoAdSpaceDAO.insertWithSellerId(sellerId, adSpace)
  }

  override def readVideoAdSpaceBySellerId(sellerId: Long): ServiceCall[NotUsed, List[VideoAdSpace]] = ServiceCall { _ =>
    videoAdSpaceDAO.findBySellerId(sellerId).map(_.toList)
  }

  override def readVideoAdSpaceById(id: Long): ServiceCall[NotUsed, Option[VideoAdSpace]] = ServiceCall { _ =>
    videoAdSpaceDAO.findById(AdSpaceId(id))
  }

  override def updateVideoAdSpace(id: Long): ServiceCall[VideoAdSpace, VideoAdSpace] = ServiceCall { adSpace =>
    videoAdSpaceDAO
      .update(adSpace.copy(id = Some(AdSpaceId(id))))
      .map(_.fold(throw NotFound(""))(b => b))
  }

  override def activateVideoAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    videoAdSpaceDAO
      .updateActive(AdSpaceId(id), active = true)
      .map(if (_) Done else throw NotFound(""))
  }

  override def deactivateVideoAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    videoAdSpaceDAO
      .updateActive(AdSpaceId(id), active = false)
      .map(if (_) Done else throw NotFound(""))
  }

  override def deleteVideoAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    videoAdSpaceDAO
      .delete(AdSpaceId(id))
      .map(if (_) Done else throw NotFound(""))
  }

  override def createNativeAdSpaceWithSellerId(sellerId: Long): ServiceCall[NativeAdSpace, NativeAdSpace] =
    ServiceCall { adSpace =>
      nativeAdSpaceDAO.insertWithSellerId(sellerId, adSpace)
    }

  override def readNativeBySellerId(sellerId: Long): ServiceCall[NotUsed, List[NativeAdSpace]] = ServiceCall { _ =>
    nativeAdSpaceDAO.findBySellerrId(sellerId).map(_.toList)
  }

  override def readNativeAdSpaceById(id: Long): ServiceCall[NotUsed, Option[NativeAdSpace]] = ServiceCall { _ =>
    nativeAdSpaceDAO.findById(AdSpaceId(id))
  }

  override def updateNativeAdSpace(id: Long): ServiceCall[NativeAdSpace, NativeAdSpace] = ServiceCall { adSpace =>
    nativeAdSpaceDAO
      .update(adSpace.copy(id = Some(AdSpaceId(id))))
      .map(_.fold(throw NotFound(""))(b => b))
  }

  override def activateNativeAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    nativeAdSpaceDAO
      .updateActive(AdSpaceId(id), active = true)
      .map(if (_) Done else throw NotFound(""))
  }

  override def deactivateNativeAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    nativeAdSpaceDAO
      .updateActive(AdSpaceId(id), active = false)
      .map(if (_) Done else throw NotFound(""))
  }

  override def deleteNativeAdSpace(id: Long): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    nativeAdSpaceDAO
      .delete(AdSpaceId(id))
      .map(if (_) Done else throw NotFound(""))
  }

  private def updateActiveStatusAgency(id: Long, newActiveStatus: Boolean): Task[Result] =
    agencyDAO
      .find(AgencyId(id))
      .flatMap(_.fold[Task[Result]](Task.pure(Results.NotFound)) { agency =>
        val agencyStatus              = agency.active.getOrElse(false)
        val agencyIsExternallyCreated = agency.externalId.isDefined

        def doNothing() = Task.pure(Results.NoContent)

        def setActivateStatusLocally(): Task[Results.Status] =
          agencyDAO
            .updateActiveStatus(agency.id.get, newActiveStatus)
            .map(b => if (b) Results.Ok else Results.InternalServerError)

        def createAndActivate = syncService.createAgency(agency).flatMap { synced =>
          if (synced) {
            setActivateStatusLocally()
          } else {
            Task.pure(Results.InternalServerError)
          }
        }

        (agencyStatus, newActiveStatus) match {
          case (true, true)                                => doNothing()
          case (true, false)                               => setActivateStatusLocally()
          case (false, false)                              => doNothing()
          case (false, true) if agencyIsExternallyCreated  => setActivateStatusLocally()
          case (false, true) if !agencyIsExternallyCreated => createAndActivate
        }
      })

  private def bothTargetingFields(bidder: Bidder): Boolean =
    bidder.excludedSellers.exists(_.nonEmpty) && bidder.includedSellers.exists(_.nonEmpty)

}
