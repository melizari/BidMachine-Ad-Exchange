package com.appodealx.exchange.settings.persistance.buyer.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.auction.{AdProfileId, BidderId}
import com.appodealx.exchange.settings.models.buyer.BannerAdProfile
import com.appodealx.exchange.settings.persistance.common.RtbBannerColumns
import com.github.zafarkhaja.semver.Version


object BannerAdProfiles extends TableQuery(new BannerAdProfiles(_)) {

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  case class LiftedBannerAdProfile(id: Rep[Option[AdProfileId]],
                                   bidderId: Rep[BidderId],
                                   title: Rep[Option[String]],
                                   active: Rep[Boolean],
                                   debug: Rep[Boolean],
                                   adChannel: Rep[Option[Int]],
                                   delayedNotification: Rep[Boolean],
                                   interstitial: Rep[Boolean],
                                   reward: Rep[Boolean],
                                   ad: LiftedRtbBanner,
                                   dmVerMax: Rep[Option[Version]],
                                   dmVerMin: Rep[Option[Version]],
                                   distributionChannel: Rep[Option[String]],
                                   template: Rep[Option[String]],
                                   allowCache: Rep[Option[Boolean]],
                                   allowCloseDelay: Rep[Option[Int]])

  implicit object BidderBannerShape extends CaseClassShape(LiftedBannerAdProfile.tupled, (BannerAdProfile.apply _).tupled)
}

class BannerAdProfiles(tag: Tag)
  extends Table[BannerAdProfile](tag, "banner_ad_profile")
    with AdProfileColumns
    with RtbBannerColumns {

  import BannerAdProfiles._

  def * = LiftedBannerAdProfile(
    id.?,
    bidderId,
    title,
    active,
    debug,
    adChannel,
    delayedNotification,
    interstitial,
    reward,
    rtbBanner,
    dmVerMax,
    dmVerMin,
    distributionChannel,
    template,
    allowCache,
    allowCloseDelay
  )

}


