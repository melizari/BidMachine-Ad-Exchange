package com.appodealx.exchange.settings.persistance.common

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.appodealx.openrtb._

trait RtbVideoColumns { self: Table[_] =>

  import com.appodealx.exchange.settings.persistance.common.LiftedRtbInstances._

  def rtbVideoMimes = column[List[String]]("rtb_video_mimes")
  def rtbVideoMinDuration = column[Option[Int]]("rtb_video_minduration")
  def rtbVideoMaxDuration = column[Option[Int]]("rtb_video_maxduration")
  def rtbVideoProtocol = column[Option[Protocol]]("rtb_video_protocol")
  def rtbVideoProtocols = column[Option[List[Protocol]]]("rtb_video_protocols")
  def rtbVideoW = column[Option[Int]]("rtb_video_w")
  def rtbVideoH = column[Option[Int]]("rtb_video_h")
  def rtbVideoStartDelay = column[Option[Int]]("rtb_video_startdelay")
  def rtbVideoLinearity = column[Option[VideoLinearity]]("rtb_video_linearity")
  def rtbVideoBattr = column[Option[List[CreativeAttribute]]]("rtb_video_battr")
  def rtbVideoMaxExtended = column[Option[Int]]("rtb_video_maxextended")
  def rtbVideoMinBitrate = column[Option[Int]]("rtb_video_minbitrate")
  def rtbVideoMaxBitrate = column[Option[Int]]("rtb_video_maxbitrate")
  def rtbVideoBoxingAllowed = column[Option[Boolean]]("rtb_video_boxingallowed")
  def rtbVideoPlaybackMethod = column[Option[List[PlaybackMethod]]]("rtb_video_playbackmethod")
  def rtbVideoDelivery = column[Option[List[ContentDeliveryMethod]]]("rtb_video_delivery")
  def rtbVideoPos = column[Option[AdPosition]]("rtb_video_pos")
  def rtbVideoApi = column[Option[List[ApiFramework]]]("rtb_video_api")
  def rtbVideoExt = column[Option[Json]]("rtb_video_ext")

  def rtbVideo = LiftedRtbVideo(
    rtbVideoMimes,
    rtbVideoMinDuration,
    rtbVideoMaxDuration,
    rtbVideoProtocol,
    rtbVideoProtocols,
    rtbVideoW,
    rtbVideoH,
    rtbVideoStartDelay,
    rtbVideoLinearity,
    rtbVideoBattr,
    rtbVideoMaxExtended,
    rtbVideoMinBitrate,
    rtbVideoMaxBitrate,
    rtbVideoBoxingAllowed,
    rtbVideoPlaybackMethod,
    rtbVideoDelivery,
    rtbVideoPos,
    rtbVideoApi,
    rtbVideoExt
  )

}
