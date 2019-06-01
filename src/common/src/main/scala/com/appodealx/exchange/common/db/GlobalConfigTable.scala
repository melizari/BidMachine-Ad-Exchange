package com.appodealx.exchange.common.db

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.exchange.common.models.GlobalConfig

class GlobalConfigTable(tag: Tag) extends Table[GlobalConfig](tag, "exchange_global_settings") {
//  def bannerExchangeFee = column[Option[Int]]("banner_exchange_fee")
//  def videoExchangeFee = column[Option[Int]]("video_exchange_fee")
//  def nativeExchangeFee = column[Option[Int]]("native_exchange_fee")
//  def interstitialExchangeFee = column[Option[Int]]("interstitial_exchange_fee")

  def tMax = column[Option[Int]]("t_max")
  def forceNoFill = column[Option[Boolean]]("debug_force_no_fill")

//  def debugTopic = column[Option[String]]("debug_bid_response_topic")
//  def debugIsInterstitial = column[Option[Boolean]]("debug_is_interstitial")
//  def debugIsBanner = column[Option[Boolean]]("debug_is_banner")
//  def debugIsVideo = column[Option[Boolean]]("debug_is_video")
//  def debugIsNative = column[Option[Boolean]]("debug_is_native")
//  def debugAppId = column[Option[Int]]("debug_app_id")
//  def debugBidderId = column[Option[Int]]("debug_bidder_id")
//  def debugOnlyBid = column[Option[Boolean]]("debug_only_bid")
//  def debugWithADM = column[Option[Boolean]]("debug_with_adm")



  def * = (
    tMax,
    forceNoFill) <> ((GlobalConfig.apply _).tupled, GlobalConfig.unapply)
}

object GlobalConfigTable extends TableQuery(new GlobalConfigTable(_))