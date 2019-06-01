package com.appodealx.openrtb.native.request

import com.appodealx.openrtb.Json
import com.appodealx.openrtb.native._

case class Data(`type`: DataType,
                len: Option[Int] = None,
                ext: Option[Json] = None)
