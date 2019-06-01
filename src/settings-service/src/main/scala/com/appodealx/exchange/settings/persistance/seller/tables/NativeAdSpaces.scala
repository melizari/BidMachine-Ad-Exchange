package com.appodealx.exchange.settings.persistance.seller.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.settings.models.seller.{AdSpaceId, NativeAdSpace}
import com.appodealx.exchange.settings.persistance.common.RtbNativeColumns

object NativeAdSpaces extends TableQuery(new NativeAdSpaces(_)) {

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  case class LiftedNativeAdSpace(id: Rep[Option[AdSpaceId]],
                                 sellerId: Rep[Option[Long]],
                                 title: Rep[Option[String]],
                                 displayManager: Rep[Option[String]],
                                 active: Rep[Boolean],
                                 debug: Rep[Boolean],
                                 adChannel: Rep[Option[Int]],
                                 interstitial: Rep[Boolean],
                                 reward: Rep[Boolean],
                                 distributionChannel: Rep[Option[String]],
                                 ad: LiftedRtbNative)

  implicit object NativeAdSpaceShape extends CaseClassShape(LiftedNativeAdSpace.tupled, NativeAdSpace.tupled)

}


class NativeAdSpaces(tag: Tag)
  extends Table[NativeAdSpace](tag, "native_ad_space")
    with AdSpaceColumns
    with RtbNativeColumns {

  import NativeAdSpaces._

  def * = LiftedNativeAdSpace(
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
    rtbNative)

}
