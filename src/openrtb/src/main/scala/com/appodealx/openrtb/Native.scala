package com.appodealx.openrtb

case class Native(request: String,
                  ver: Option[String] = None,
                  api: Option[List[ApiFramework]] = None,
                  battr: Option[List[CreativeAttribute]] = None,
                  ext: Option[Json] = None)
