package com.appodealx.openrtb

case class Pmp(privateAuction: Option[Boolean] = None,
               deals: Option[List[Deal]] = None,
               ext: Option[Json] = None)
