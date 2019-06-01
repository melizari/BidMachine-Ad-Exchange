package com.appodealx.openrtb

case class Data(id: Option[String] = None,
                name: Option[String] = None,
                segment: Option[List[Segment]] = None,
                ext: Option[Json] = None)
