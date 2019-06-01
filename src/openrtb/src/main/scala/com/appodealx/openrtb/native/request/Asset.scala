package com.appodealx.openrtb.native.request

import com.appodealx.openrtb.Json


case class Asset(id: Int,
                 required: Option[Boolean] = Some(false),
                 title: Option[Title] = None,
                 img: Option[Image] = None,
                 video: Option[Video] = None,
                 data: Option[Data] = None,
                 ext: Option[Json] = None)
