package com.appodealx.openrtb

case class Format(w: Option[Int] = None,
                  h: Option[Int] = None,
                  wratio: Option[Int] = None,
                  hratio: Option[Int] = None,
                  wmin: Option[Int] = None,
                  ext: Option[Json] = None)