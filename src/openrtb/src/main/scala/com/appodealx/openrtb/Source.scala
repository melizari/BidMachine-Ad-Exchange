package com.appodealx.openrtb

case class Source(fd: Option[Int] = None,
                  tid: Option[String] = None,
                  pchain: Option[String] = None,
                  ext: Option[Json] = None)