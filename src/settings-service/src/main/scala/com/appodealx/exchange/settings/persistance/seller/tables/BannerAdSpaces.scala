package com.appodealx.exchange.settings.persistance.seller.tables

import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._
import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.settings.models.seller.{AdSpaceId, BannerAdSpace}
import com.appodealx.exchange.settings.persistance.common.RtbBannerColumns

object BannerAdSpaces extends TableQuery(new BannerAdSpaces(_)) {


case class LiftedBannerAdSpace(id: Rep[Option[AdSpaceId]],
                               sellerId: Rep[Option[Long]],
                               title: Rep[Option[String]],
                               displayManager: Rep[Option[String]],
                               active: Rep[Boolean],
                               debug: Rep[Boolean],
                               adChannel: Rep[Option[Int]],
                               interstitial: Rep[Boolean],
                               reward: Rep[Boolean],
                               distributionChannel: Rep[Option[String]],
                               ad: LiftedRtbBanner)

implicit object BannerAdSpaceShape extends CaseClassShape(LiftedBannerAdSpace.tupled, BannerAdSpace.tupled)

}


class BannerAdSpaces(tag: Tag)
  extends Table[BannerAdSpace](tag, "banner_ad_space")
    with AdSpaceColumns
    with RtbBannerColumns {

  import BannerAdSpaces._

  def * = LiftedBannerAdSpace(
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
    rtbBanner)

}