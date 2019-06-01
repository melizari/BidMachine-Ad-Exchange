package com.appodealx.openrtb.native.request

import com.appodealx.openrtb.Json
import com.appodealx.openrtb.native.ImageType

case class Image(`type`: Option[ImageType] = None,
                 w: Option[Int] = None,
                 wmin: Option[Int] = None,
                 h: Option[Int] = None,
                 hmin: Option[Int] = None,
                 mimes: Option[List[String]] = None,
                 ext: Option[Json] = None)
