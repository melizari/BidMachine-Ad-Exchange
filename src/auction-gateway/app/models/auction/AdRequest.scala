package models.auction

import com.appodealx.exchange.settings.models.seller.AdSpaceId
import com.appodealx.openrtb.{App, AuctionType, Device, Regs, User}
import models.RequestHost

case class AdRequest[P](id: String,
                        impId: Option[String],
                        dcid: Option[String],
                        sellerBidFloor: Double,
                        bidFloor: Double,
                        ad: P,
                        adSpaceId: Option[AdSpaceId] = None,
                        app: App,
                        device: Device,
                        user: User,
                        coppa: Option[Boolean] = None,
                        test: Option[Boolean] = None,
                        adUnits: List[AdUnit],
                        tmax: Option[Int],
                        interstitial: Boolean,
                        reward: Boolean,
                        debug: Boolean,
                        adChannel: Option[Int] = None,
                        sdk: Option[String],
                        sdkVersion: Option[String],
                        externalCampaignImageId: Option[Long],
                        metadata: Boolean = false,
                        sellerId: Option[Long],
                        sellerName: Option[String],
                        sellerFee: Option[Double] = None,
                        gdpr: Option[Boolean] = None,
                        consent: Option[String] = None,
                        at: Option[AuctionType] = None,
                        sesN: Option[Long] = None, //For PB only
                        impN: Option[Long] = None, //For PB only
                        host: RequestHost,
                        bcat: Option[List[String]] = None,
                        badv: Option[List[String]] = None,
                        bapp: Option[List[String]] = None)