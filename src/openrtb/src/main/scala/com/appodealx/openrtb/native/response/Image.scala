package com.appodealx.openrtb.native.response

import com.appodealx.openrtb.{Json, Url}


case class Image(url: Url,
                 w: Option[Int] = None,
                 h: Option[Int] = None,
                 ext: Option[Json] = None)