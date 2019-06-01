package com.appodealx.exchange.settings.persistance.seller.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.settings.models.seller.{AdSpaceId, VideoAdSpace}
import com.appodealx.exchange.settings.persistance.common.RtbVideoColumns

object VideoAdSpaces extends TableQuery(new VideoAdSpaces(_)) {

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  case class LiftedVideoAdSpace(id: Rep[Option[AdSpaceId]],
                                sellerId: Rep[Option[Long]],
                                title: Rep[Option[String]],
                                displayManager: Rep[Option[String]],
                                active: Rep[Boolean],
                                debug: Rep[Boolean],
                                adChannel: Rep[Option[Int]],
                                interstitial: Rep[Boolean],
                                reward: Rep[Boolean],
                                distributionChannel: Rep[Option[String]],
                                ad: LiftedRtbVideo)

  implicit object VideoAdSpaceShape extends CaseClassShape(LiftedVideoAdSpace.tupled, VideoAdSpace.tupled)

}


class VideoAdSpaces(tag: Tag)
  extends Table[VideoAdSpace](tag, "video_ad_space")
    with AdSpaceColumns
    with RtbVideoColumns {

  import VideoAdSpaces._

  def * = LiftedVideoAdSpace(
    id.?,
    sellerId,
    title,
    displayManager,
    active,
    debug,
    adChannel,
    interstitial,
    reward,
    distributionChannel,
    rtbVideo)

}