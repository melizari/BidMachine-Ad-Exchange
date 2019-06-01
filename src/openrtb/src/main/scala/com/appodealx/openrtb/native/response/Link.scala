package com.appodealx.openrtb.native.response

import com.appodealx.openrtb.{Json, Url}


case class Link(url: Url,
                clicktrackers: Option[List[Url]] = None,
                fallback: Option[Url] = None,
                ext: Option[Json])
