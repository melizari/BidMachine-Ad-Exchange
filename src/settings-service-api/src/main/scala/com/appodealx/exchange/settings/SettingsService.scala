package com.appodealx.exchange.settings

import akka.{Done, NotUsed}
import com.appodealx.exchange.common.models.auction.{Agency, Bidder}
import com.appodealx.exchange.settings.models.buyer._
import com.appodealx.exchange.settings.models.circe.{CirceBuyerSettingsInstances, CirceSellerSettingsInstances}
import com.appodealx.exchange.settings.models.seller._
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}


abstract class SettingsService extends Service with CirceBuyerSettingsInstances with CirceSellerSettingsInstances{

  def readAllAgencies: ServiceCall[NotUsed, List[Agency]]
  def readAgency(id: Long): ServiceCall[NotUsed, Agency]
  def createAgency: ServiceCall[Agency, Agency]
  def updateAgency(id: Long): ServiceCall[Agency, Agency]
  def deleteAgency(id: Long): ServiceCall[NotUsed, Done]
  def activateAgency(id: Long): ServiceCall[NotUsed, Done]
  def deactivateAgency(id: Long): ServiceCall[NotUsed, Done]

  def readBiddersByAgencyId(id: Long): ServiceCall[NotUsed, List[Bidder]]

  def readAllBidders: ServiceCall[NotUsed, List[Bidder]]
  def readBidder(id: Long): ServiceCall[NotUsed, Bidder]
  def createBidder: ServiceCall[Bidder, Bidder]
  def updateBidder(id: Long): ServiceCall[Bidder, Bidder]
  def deleteBidder(id: Long): ServiceCall[NotUsed, Done]

  def readBannerAdProfiles(id: Long): ServiceCall[NotUsed, List[BannerAdProfile]]
  def readVideoAdProfiles(id: Long): ServiceCall[NotUsed, List[VideoAdProfile]]
  def readNativeAdProfiles(id: Long): ServiceCall[NotUsed, List[NativeAdProfile]]

  def readAllBannerAdProfiles: ServiceCall[NotUsed, List[BannerAdProfileWithBidder]]
  def readBannerAdProfile(id: Long): ServiceCall[NotUsed, BannerAdProfile]
  def createBannerAdProfile: ServiceCall[BannerAdProfile, BannerAdProfile]
  def updateBannerAdProfile(id: Long): ServiceCall[BannerAdProfile, BannerAdProfile]
  def deleteBannerAdProfile(id: Long): ServiceCall[NotUsed, Done]
  def activateBannerAdProfile(id: Long): ServiceCall[NotUsed, Done]
  def deactivateBannerAdProfile(id: Long): ServiceCall[NotUsed, Done]

  def readAllVideoAdProfiles: ServiceCall[NotUsed, List[VideoAdProfileWithBidder]]
  def readVideoAdProfile(id: Long): ServiceCall[NotUsed, VideoAdProfile]
  def createVideoAdProfile: ServiceCall[VideoAdProfile, VideoAdProfile]
  def updateVideoAdProfile(id: Long): ServiceCall[VideoAdProfile, VideoAdProfile]
  def deleteVideoAdProfile(id: Long): ServiceCall[NotUsed, Done]
  def activateVideoAdProfile(id: Long): ServiceCall[NotUsed, Done]
  def deactivateVideoAdProfile(id: Long): ServiceCall[NotUsed, Done]

  def readAllNativeAdProfiles: ServiceCall[NotUsed, List[NativeAdProfileWithBidder]]
  def readNativeAdProfile(id: Long): ServiceCall[NotUsed, NativeAdProfile]
  def createNativeAdProfile: ServiceCall[NativeAdProfile, NativeAdProfile]
  def updateNativeAdProfile(id: Long): ServiceCall[NativeAdProfile, NativeAdProfile]
  def deleteNativeAdProfile(id: Long): ServiceCall[NotUsed, Done]
  def activateNativeAdProfile(id: Long): ServiceCall[NotUsed, Done]
  def deactivateNativeAdProfile(id: Long): ServiceCall[NotUsed, Done]

  // id - internal id
  // eid - external id
  def findAllSellers: ServiceCall[NotUsed, List[Seller]]
  def findSeller(id: Long): ServiceCall[NotUsed, Seller]
  def insertSeller: ServiceCall[Seller, Seller]
  def updateSeller(id: Long): ServiceCall[Seller, Seller]
  def deleteSeller(id: Long): ServiceCall[NotUsed, Done]

  def createBannerAdSpaceWithSellerId(sellerId: Long): ServiceCall[BannerAdSpace, BannerAdSpace]
  def readBannerBySellerId(sellerId: Long): ServiceCall[NotUsed, List[BannerAdSpace]]
  def readBannerAdSpaceById(id: Long): ServiceCall[NotUsed, Option[BannerAdSpace]]
  def updateBannerAdSpace(id: Long): ServiceCall[BannerAdSpace, BannerAdSpace]
  def activateBannerAdSpace(id: Long): ServiceCall[NotUsed, Done]
  def deactivateBannerAdSpace(id: Long): ServiceCall[NotUsed, Done]
  def deleteBannerAdSpace(id: Long): ServiceCall[NotUsed, Done]

  def createVideoAdSpaceWithSellerId(sellerId: Long): ServiceCall[VideoAdSpace, VideoAdSpace]
  def readVideoAdSpaceBySellerId(sellerId: Long): ServiceCall[NotUsed, List[VideoAdSpace]]
  def readVideoAdSpaceById(id: Long): ServiceCall[NotUsed, Option[VideoAdSpace]]
  def updateVideoAdSpace(id: Long): ServiceCall[VideoAdSpace, VideoAdSpace]
  def activateVideoAdSpace(id: Long): ServiceCall[NotUsed, Done]
  def deactivateVideoAdSpace(id: Long): ServiceCall[NotUsed, Done]
  def deleteVideoAdSpace(id: Long): ServiceCall[NotUsed, Done]

  def createNativeAdSpaceWithSellerId(sellerId: Long): ServiceCall[NativeAdSpace, NativeAdSpace]
  def readNativeBySellerId(sellerId: Long): ServiceCall[NotUsed, List[NativeAdSpace]]
  def readNativeAdSpaceById(id: Long): ServiceCall[NotUsed, Option[NativeAdSpace]]
  def updateNativeAdSpace(id: Long): ServiceCall[NativeAdSpace, NativeAdSpace]
  def activateNativeAdSpace(id: Long): ServiceCall[NotUsed, Done]
  def deactivateNativeAdSpace(id: Long): ServiceCall[NotUsed, Done]
  def deleteNativeAdSpace(id: Long): ServiceCall[NotUsed, Done]


  def status: ServiceCall[NotUsed, Done]

  def descriptor: Descriptor = {

    import Service._
    import com.appodealx.exchange.common.utils.circe.CirceLagomMessageSerializer._

    named("settings-service")
      .addCalls(

        restCall(Method.GET, "/settings/status", status),

        //buyer settings
        restCall(Method.GET, "/agency", readAllAgencies),
        restCall(Method.GET, "/agency/:id", readAgency _),
        restCall(Method.POST, "/agency", createAgency),
        restCall(Method.POST, "/agency/:id", updateAgency _),
        restCall(Method.DELETE, "/agency/:id", deleteAgency _),
        restCall(Method.POST, "/agency/:id/active", activateAgency _),
        restCall(Method.DELETE, "/agency/:id/active", deactivateAgency _),
        restCall(Method.GET, "/agency/:id/bidder", readBiddersByAgencyId _),

        restCall(Method.GET, "/bidder", readAllBidders),
        restCall(Method.GET, "/bidder/:id", readBidder _),
        restCall(Method.POST, "/bidder", createBidder),
        restCall(Method.POST, "/bidder/:id", updateBidder _),
        restCall(Method.DELETE, "/bidder/:id", deleteBidder _),

        restCall(Method.GET, "/bidder/:id/adprofile/banner", readBannerAdProfiles _),
        restCall(Method.GET, "/bidder/:id/adprofile/video", readVideoAdProfiles _),
        restCall(Method.GET, "/bidder/:id/adprofile/native", readNativeAdProfiles _),

        restCall(Method.GET, "/adprofile/banner", readAllBannerAdProfiles),
        restCall(Method.GET, "/adprofile/banner/:id", readBannerAdProfile _),
        restCall(Method.POST, "/adprofile/banner", createBannerAdProfile),
        restCall(Method.POST, "/adprofile/banner/:id", updateBannerAdProfile _),
        restCall(Method.DELETE, "/adprofile/banner/:id", deleteBannerAdProfile _),
        restCall(Method.POST, "/adprofile/banner/:id/active", activateBannerAdProfile _),
        restCall(Method.DELETE, "/adprofile/banner/:id/active", deactivateBannerAdProfile _),

        restCall(Method.GET, "/adprofile/video", readAllVideoAdProfiles),
        restCall(Method.GET, "/adprofile/video/:id", readVideoAdProfile _),
        restCall(Method.POST, "/adprofile/video", createVideoAdProfile),
        restCall(Method.POST, "/adprofile/video/:id", updateVideoAdProfile _),
        restCall(Method.DELETE, "/adprofile/video/:id", deleteVideoAdProfile _),
        restCall(Method.POST, "/adprofile/video/:id/active", activateVideoAdProfile _),
        restCall(Method.DELETE, "/adprofile/video/:id/active", deactivateVideoAdProfile _),

        restCall(Method.GET, "/adprofile/native", readAllNativeAdProfiles),
        restCall(Method.GET, "/adprofile/native/:id", readNativeAdProfile _),
        restCall(Method.POST, "/adprofile/native", createNativeAdProfile),
        restCall(Method.POST, "/adprofile/native/:id", updateNativeAdProfile _),
        restCall(Method.DELETE, "/adprofile/native/:id", deleteNativeAdProfile _),
        restCall(Method.POST, "/adprofile/native/:id/active", activateNativeAdProfile _),
        restCall(Method.DELETE, "/adprofile/native/:id/active", deactivateNativeAdProfile _),

        // seller's settings
        restCall(Method.GET, "/api/v1/sellers", findAllSellers),
        restCall(Method.GET, "/api/v1/sellers/:id", findSeller _),
        restCall(Method.POST, "/api/v1/sellers", insertSeller),
        restCall(Method.PUT, "/api.v1.sellers/:id", updateSeller _),
        restCall(Method.DELETE, "/api/v1/sellers/:id", deleteSeller _),

        restCall(Method.POST, "/adspace/banner/:id", updateBannerAdSpace _),
        restCall(Method.DELETE, "/adspace/banner/:id", deleteBannerAdSpace _),
        restCall(Method.POST, "/adspace/banner/:id/active", activateBannerAdSpace _),
        restCall(Method.DELETE, "/adspace/banner/:id/active", deactivateBannerAdSpace _),

        restCall(Method.POST, "/adspace/video/:id", updateVideoAdSpace _),
        restCall(Method.DELETE, "/adspace/video/:id", deleteVideoAdSpace _),
        restCall(Method.POST, "/adspace/video/:id/active", activateVideoAdSpace _),
        restCall(Method.DELETE, "/adspace/video/:id/active", deactivateVideoAdSpace _),

        restCall(Method.POST, "/adspace/native/:id", updateNativeAdSpace _),
        restCall(Method.DELETE, "/adspace/native/:id", deleteNativeAdSpace _),
        restCall(Method.POST, "/adspace/native/:id/active", activateNativeAdSpace _),
        restCall(Method.DELETE, "/adspace/native/:id/active", deactivateNativeAdSpace _),

        restCall(Method.GET, "/seller/:id/adspace/banner", readBannerBySellerId _),
        restCall(Method.GET, "/adspace/banner/:id", readBannerAdSpaceById _),
        restCall(Method.POST, "/seller/:id/adspace/banner", createBannerAdSpaceWithSellerId _),
        restCall(Method.GET, "/seller/:id/adspace/video", readVideoAdSpaceBySellerId _),
        restCall(Method.GET, "/adspace/video/:id", readVideoAdSpaceById _),
        restCall(Method.POST, "/seller/:id/adspace/video", createVideoAdSpaceWithSellerId _),
        restCall(Method.GET, "/seller/:id/adspace/native", readNativeBySellerId _),
        restCall(Method.GET, "/adspace/native/:id", readNativeAdSpaceById _),
        restCall(Method.POST, "/seller/:id/adspace/native", createNativeAdSpaceWithSellerId _)
      ).withAutoAcl(true)
  }
}
