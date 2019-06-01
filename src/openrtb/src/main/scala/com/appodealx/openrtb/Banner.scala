package com.appodealx.openrtb

case class Banner(w: Option[Int] = None,
                  h: Option[Int] = None,
                  format: Option[List[Format]] = None,
                  id: Option[String] = None,
                  btype: Option[List[BannerAdType]] = None,
                  battr: Option[List[CreativeAttribute]] = None,
                  pos: Option[AdPosition] = None,
                  mimes: Option[List[String]] = None,
                  topframe: Option[Boolean] = None,
                  expdir: Option[List[ExpandableDirection]] = None,
                  api: Option[List[ApiFramework]] = None,
                  vcm: Option[Int] = None,
                  ext: Option[Json] = None)
