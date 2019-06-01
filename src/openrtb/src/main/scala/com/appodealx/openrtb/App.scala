package com.appodealx.openrtb

case class App(id: Option[String] = None,
               name: Option[String] = None,
               bundle: Option[String] = None,
               domain: Option[String] = None,
               storeurl: Option[String] = None,
               cat: Option[List[String]] = None,
               sectioncat: Option[List[String]] = None,
               pagecat: Option[List[String]] = None,
               ver: Option[String] = None,
               privacypolicy: Option[Boolean] = None,
               paid: Option[Boolean] = None,
               publisher: Option[Publisher] = None,
               content: Option[Content] = None,
               keywords: Option[String] = None,
               ext: Option[Json] = None)
