package com.appodealx.openrtb

case class Metric(`type`: String,
                  value: Double,
                  vendor: Option[String] = None,
                  ext: Option[Json] = None)
