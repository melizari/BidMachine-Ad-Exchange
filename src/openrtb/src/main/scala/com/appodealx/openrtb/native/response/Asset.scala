package com.appodealx.openrtb.native.response

import com.appodealx.openrtb.Json

case class Asset(id: Int,
                 required: Option[Boolean] = Some(false),
                 title: Option[Title] = None,
                 img: Option[Image] = None,
                 video: Option[Video] = None,
                 data: Option[Data] = None,
                 link: Option[Link] = None,
                 ext: Option[Json] = None)
