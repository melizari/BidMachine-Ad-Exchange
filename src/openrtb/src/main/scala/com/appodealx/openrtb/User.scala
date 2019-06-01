package com.appodealx.openrtb

case class User(id: Option[String] = None,
                buyeruid: Option[String] = None,
                yob: Option[Int] = None,
                gender: Option[Gender] = None,
                keywords: Option[String] = None,
                customdata: Option[String] = None,
                geo: Option[Geo] = None,
                data: Option[List[Data]] = None,
                ext: Option[Json] = None)