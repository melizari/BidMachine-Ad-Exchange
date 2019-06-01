package com.appodealx.openrtb.native.response

import com.appodealx.openrtb.Json

case class Data(label: Option[String],
                value: String,
                ext: Option[Json] = None)