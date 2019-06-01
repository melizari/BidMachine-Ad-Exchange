package com.appodealx.exchange.settings.persistance.common

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.openrtb._

trait RtbBannerColumns { self: Table[_] =>

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  def rtbBannerW = column[Option[Int]]("rtb_banner_w")
  def rtbBannerH = column[Option[Int]]("rtb_banner_h")
  def rtbBannerBtype = column[Option[List[BannerAdType]]]("rtb_banner_btype")
  def rtbBannerBattr = column[Option[List[CreativeAttribute]]]("rtb_banner_battr")
  def rtbBannerPos = column[Option[AdPosition]]("rtb_banner_pos")
  def rtbBannerMimes = column[Option[List[String]]]("rtb_banner_mimes")
  def rtbBannerTopframe = column[Option[Boolean]]("rtb_banner_topframe")
  def rtbBannerExpdir = column[Option[List[ExpandableDirection]]]("rtb_banner_expdir")
  def rtbBannerApi = column[Option[List[ApiFramework]]]("rtb_banner_api")
  def rtbBannerExt = column[Option[Json]]("rtb_banner_ext")

  def rtbBanner = LiftedRtbBanner(
    rtbBannerW,
    rtbBannerH,
    rtbBannerBtype,
    rtbBannerBattr,
    rtbBannerPos,
    rtbBannerMimes,
    rtbBannerTopframe,
    rtbBannerExpdir,
    rtbBannerApi,
    rtbBannerExt
  )
}
