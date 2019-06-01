package com.appodealx.exchange.settings.persistance.buyer.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.auction.{AdProfileId, BidderId}
import com.appodealx.exchange.settings.models.buyer.NativeAdProfile
import com.appodealx.exchange.settings.persistance.common.RtbNativeColumns
import com.github.zafarkhaja.semver.Version


object NativeAdProfiles extends TableQuery(new NativeAdProfiles(_)) {

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  case class LiftedNativeAdProfile(id: Rep[Option[AdProfileId]],
                                   bidderId: Rep[BidderId],
                                   title: Rep[Option[String]],
                                   active: Rep[Boolean],
                                   debug: Rep[Boolean],
                                   adChannel: Rep[Option[Int]],
                                   delayedNotification: Rep[Boolean],
                                   interstitial: Rep[Boolean],
                                   reward: Rep[Boolean],
                                   ad: LiftedRtbNative,
                                   dmVerMax: Rep[Option[Version]],
                                   dmVerMin: Rep[Option[Version]],
                                   distributionChannel: Rep[Option[String]],
                                   template: Rep[Option[String]],
                                   allowCache: Rep[Option[Boolean]],
                                   allowCloseDelay: Rep[Option[Int]])

  implicit object NativeAdProfileShape extends CaseClassShape(LiftedNativeAdProfile.tupled, (NativeAdProfile.apply _).tupled)

}

class NativeAdProfiles(tag: Tag) extends Table[NativeAdProfile](tag, "native_ad_profile")
  with AdProfileColumns
  with RtbNativeColumns {

  import NativeAdProfiles._

  def * = LiftedNativeAdProfile(
    id.?,
    bidderId,
    title,
    active,
    debug,
    adChannel,
    delayedNotification,
    interstitial,
    reward,
    rtbNative,
    dmVerMax,
    dmVerMin,
    distributionChannel,
    template,
    allowCache,
    allowCloseDelay)


}
