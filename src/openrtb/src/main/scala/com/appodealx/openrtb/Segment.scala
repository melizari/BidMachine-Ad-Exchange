package com.appodealx.openrtb

case class Segment(id: Option[String] = None,
                   name: Option[String] = None,
                   value: Option[String] = None,
                   ext: Option[Json] = None)
