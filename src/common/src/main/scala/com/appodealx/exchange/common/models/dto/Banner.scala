package com.appodealx.exchange.common.models.dto

import com.appodealx.openrtb
import com.appodealx.openrtb._

case class Banner(w: Option[Int],
                  h: Option[Int],
                  btype: Option[List[BannerAdType]],
                  battr: Option[List[CreativeAttribute]],
                  pos: Option[AdPosition],
                  mimes: Option[List[String]],
                  topframe: Option[Boolean],
                  expdir: Option[List[ExpandableDirection]],
                  api: Option[List[ApiFramework]],
                  ext: Option[Json] = None) { self =>


  def withDefaults = copy(
      mimes = self.mimes.orElse(Some(List("image/jpeg", "image/jpg", "image/gif", "image/png"))),
      api = self.api.orElse(Some(ApiFramework.values.toList))
    )

  def toRtb = openrtb.Banner(
    w = w,
    h = h,
    btype = btype,
    battr = battr,
    pos = pos,
    mimes = mimes,
    topframe = topframe,
    expdir = expdir,
    api = api,
    ext = ext
  )
}