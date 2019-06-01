package com.appodealx.openrtb

case class Site(id: Option[String] = None,
                name: Option[String] = None,
                domain: Option[String] = None,
                cat: Option[List[String]] = None,
                sectioncat: Option[List[String]] = None,
                pagecat: Option[List[String]] = None,
                page: Option[String] = None,
                ref: Option[String] = None,
                search: Option[String] = None,
                mobile: Option[Boolean] = None,
                privacypolicy: Option[Boolean] = None,
                publisher: Option[Publisher] = None,
                content: Option[Content] = None,
                keywords: Option[String] = None,
                ext: Option[Json] = None)
