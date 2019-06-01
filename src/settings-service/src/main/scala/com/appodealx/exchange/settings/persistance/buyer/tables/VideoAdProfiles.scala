package com.appodealx.exchange.settings.persistance.buyer.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.auction.{AdProfileId, BidderId}
import com.appodealx.exchange.settings.models.buyer.VideoAdProfile
import com.appodealx.exchange.settings.persistance.common.RtbVideoColumns
import com.github.zafarkhaja.semver.Version
import slick.lifted.CaseClassShape


object VideoAdProfiles extends TableQuery(new VideoAdProfiles(_)) {

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  case class LiftedVideoAdProfile(id: Rep[Option[AdProfileId]],
                                  bidderId: Rep[BidderId],
                                  title: Rep[Option[String]],
                                  active: Rep[Boolean],
                                  debug: Rep[Boolean],
                                  adChannel: Rep[Option[Int]],
                                  delayedNotification: Rep[Boolean],
                                  interstitial: Rep[Boolean],
                                  reward: Rep[Boolean],
                                  ad: LiftedRtbVideo,
                                  dmVerMax: Rep[Option[Version]],
                                  dmVerMin: Rep[Option[Version]],
                                  distributionChannel: Rep[Option[String]],
                                  template: Rep[Option[String]],
                                  allowCache: Rep[Option[Boolean]],
                                  allowCloseDelay: Rep[Option[Int]])

  implicit object VideoAdProfileShape extends CaseClassShape(LiftedVideoAdProfile.tupled, (VideoAdProfile.apply _).tupled)

}

class VideoAdProfiles(tag: Tag)
  extends Table[VideoAdProfile](tag, "video_ad_profile")
    with AdProfileColumns
    with RtbVideoColumns {

  import VideoAdProfiles._

  def * = LiftedVideoAdProfile(
    id.?,
    bidderId,
    title,
    active,
    debug,
    adChannel,
    delayedNotification,
    interstitial,
    reward,
    rtbVideo,
    dmVerMax,
    dmVerMin,
    distributionChannel,
    template,
    allowCache,
    allowCloseDelay)

}