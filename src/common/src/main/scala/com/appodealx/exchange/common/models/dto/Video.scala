package com.appodealx.exchange.common.models.dto

import com.appodealx.openrtb
import com.appodealx.openrtb._

case class Video(mimes: List[String],
                 minduration: Option[Int] = None,
                 maxduration: Option[Int] = None,
                 protocol: Option[Protocol] = None,
                 protocols: Option[List[Protocol]] = None,
                 w: Option[Int] = None,
                 h: Option[Int] = None,
                 startdelay: Option[Int] = None,
                 linearity: Option[VideoLinearity] = None,
                 battr: Option[List[CreativeAttribute]] = None,
                 maxextended: Option[Int] = None,
                 minbitrate: Option[Int] = None,
                 maxbitrate: Option[Int] = None,
                 boxingallowed: Option[Boolean] = Some(true),
                 playbackmethod: Option[List[PlaybackMethod]] = None,
                 delivery: Option[List[ContentDeliveryMethod]] = None,
                 pos: Option[AdPosition] = None,
                 api: Option[List[ApiFramework]] = None,
                 ext: Option[Json] = None) { self =>

  def withDefaults = copy(
    protocols = self.protocols.orElse(Some(Protocol.values.toList)),
    linearity = self.linearity.orElse(Some(VideoLinearity.Linear)),
    api = self.api.orElse(Some(ApiFramework.values.toList))
  )

  def toRtb = openrtb.Video(
    mimes = mimes.toList,
    minduration = minduration,
    maxduration = maxduration,
    protocol = protocol,
    protocols = protocols.map(_.toList),
    w = w,
    h = h,
    startdelay = startdelay,
    linearity = linearity,
    battr = battr.map(_.toList),
    maxextended = maxextended,
    minbitrate = minbitrate,
    maxbitrate = maxbitrate,
    boxingallowed = boxingallowed,
    playbackmethod = playbackmethod.map(_.toList),
    delivery = delivery.map(_.toList),
    pos = pos,
    api = api.map(_.toList),
    ext = ext
  )
}