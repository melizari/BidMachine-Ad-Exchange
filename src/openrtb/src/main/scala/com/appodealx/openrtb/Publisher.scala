package com.appodealx.openrtb

case class Publisher(id: Option[String] = None,
                     name: Option[String] = None,
                     cat: Option[List[String]] = None,
                     domain: Option[String] = None,
                     ext: Option[Json] = None)