package com.appodealx.openrtb

case class Content(id: Option[String] = None,
                   episode: Option[Int] = None,
                   title: Option[String] = None,
                   series: Option[String] = None,
                   season: Option[String] = None,
                   artist: Option[String] = None,
                   genre: Option[String] = None,
                   album: Option[String] = None,
                   isrc: Option[String] = None,
                   producer: Option[Producer] = None,
                   url: Option[Url] = None,
                   cat: Option[List[String]] = None,
                   prodq: Option[ProductionQuality] = None,
                   videoquality: Option[ProductionQuality] = None,
                   context: Option[ContentContext] = None,
                   contentrating: Option[String] = None,
                   userrating: Option[String] = None,
                   qagmediarating: Option[QagMediaRating] = None,
                   keywords: Option[String] = None,
                   livestream: Option[Boolean] = None,
                   sourcerelationship: Option[Int] = None,
                   len: Option[Int] = None,
                   lanuage: Option[String] = None,
                   embeddable: Option[Boolean] = None,
                   data: Option[List[Data]] = None,
                   ext: Option[Json] = None)