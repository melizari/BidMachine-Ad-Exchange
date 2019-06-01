package com.appodealx.exchange.common.models.analytics


case class ContextTrackers(nurl: Option[String] = None,
                           burl: Option[String] = None,
                           impTrackers: List[String] = Nil,
                           clickTrackers: List[String] = Nil)