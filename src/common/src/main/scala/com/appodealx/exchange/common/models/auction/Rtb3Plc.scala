package com.appodealx.exchange.common.models.auction

import com.appodealx.exchange.common.mappers.NativeRequestMapper
import com.appodealx.exchange.common.models.circe.CirceRtbInstances
import com.appodealx.exchange.common.models.dto.{Banner, BannerAndVideo, Native, Video}
import com.appodealx.exchange.common.models.jsoniter.JsoniterRtbInstances
import com.appodealx.exchange.common.utils._
import com.appodealx.openrtb.CreativeAttribute
import com.github.plokhotnyuk.jsoniter_scala.core.writeToArray
import io.bidmachine.protobuf.adcom
import io.bidmachine.protobuf.adcom.Context.Device
import io.bidmachine.protobuf.adcom.Placement.DisplayPlacement
import io.bidmachine.protobuf.adcom.{Context, DeviceType, Placement}
import io.circe.Json

trait Rtb3Plc[P] {

  def fromRtb3(repr: (adcom.Placement, Context)): Option[P]

}

object Rtb3Plc {

  def apply[P: Rtb3Plc] = implicitly[Rtb3Plc[P]]

  implicit object BannerRtb3Plc extends Rtb3Plc[Banner] {

    import CreativeAttribute._

    private val defaultBattr = Set(
      ExpandableUserRollover,
      Pop,
      Provocative,
      EpilepsyWarning,
      AlertStyle
    )

    private val leaderboardBattr = Set(
      AudioAdAutoPlay,
      AudioAdUserInit,
      ExpandableAutomatic,
      ExpandableUserClick,
      InBannerVideoAdAutoPlay,
      InBannerVideoAdUserInit,
      HasAudionButton
    )

    override def fromRtb3(repr: (adcom.Placement, Context)) = {
      def isLeaderboardSize(dp: DisplayPlacement) = {
        val mobileLeaderboard = dp.w == 320 && dp.h == 50
        val tabletLeaderboard = dp.w == 728 && dp.h == 90

        mobileLeaderboard || tabletLeaderboard
      }

      val (plc, ctx) = repr

      val isInterstitial = plc.display.exists(_.instl)
      val isLeaderboard  = !isInterstitial && plc.display.exists(isLeaderboardSize)

      val battr = {
        val builder = Set.newBuilder[CreativeAttribute]
        builder ++= plc.battr.flatMap(_.convertToRtb)
        builder ++= defaultBattr
        if (plc.reward) builder += CreativeAttribute.AdCanBeSkipped
        if (isLeaderboard) builder ++= leaderboardBattr
        builder.result.toList
      }

      val ext = if (plc.reward && isInterstitial) {
        Some(Json.obj("bannertype" -> Json.fromString("rewarded")))
      } else {
        None
      }

      def displaySize(dp: DisplayPlacement, device: Device) =
        if (dp.instl) {
          Some(formatSize((dp.w, dp.h), device.`type`))
        } else {
          Some((dp.w, dp.h))
        }

      for {
        display <- plc.display
        device  <- ctx.device
        (w, h)  <- displaySize(display, device)
      } yield {
        Banner(
          w = Some(w),
          h = Some(h),
          btype = None,
          battr = Some(battr),
          pos = None,
          mimes = None,
          topframe = None,
          expdir = None,
          api = None,
          ext = ext
        ).withDefaults
      }
    }
  }

  implicit object VideoRtb3Plc extends Rtb3Plc[Video] {
    override def fromRtb3(repr: (adcom.Placement, Context)): Option[Video] = {
      val (plc, ctx) = repr

      val videoType = Json.fromString(if (plc.reward) "rewarded" else "skippable")

      val battr = {
        val builder = Set.newBuilder[CreativeAttribute]
        builder ++= plc.battr.flatMap(_.convertToRtb)
        if (plc.reward) builder += CreativeAttribute.AdCanBeSkipped
        builder.result.toList
      }

      for {
        video  <- plc.video
        device <- ctx.device
        (w, h) = formatSize((video.w, video.h), device.`type`)
      } yield {
        Video(
          w = Some(w),
          h = Some(h),
          minduration = Some(video.mindur),
          maxduration = Some(video.maxdur),
          mimes = video.mime.toList,
          battr = Some(battr),
          ext = Some(Json.obj("videotype" -> videoType))
        ).withDefaults
      }
    }
  }

  implicit object NativeRtb3Plc extends Rtb3Plc[Native] with CirceRtbInstances with JsoniterRtbInstances {
    override def fromRtb3(repr: (adcom.Placement, Context)): Option[Native] =
      for {
        dsp <- repr._1.display
        nrq <- NativeRequestMapper.toRtb2(dsp)
      } yield
        Native(
          battr = Some(repr._1.battr.toList.flatMap(_.convertToRtb)),
          request = Some(new String(writeToArray(nrq)))
        ).withDefaults
  }

  implicit object BannerAndVideoRtb3Plc extends Rtb3Plc[BannerAndVideo] with CirceRtbInstances with JsoniterRtbInstances {
    override def fromRtb3(repr: (Placement, Context)): Option[(Banner, Video)] =
      for {
        banner <- BannerRtb3Plc.fromRtb3(repr)
        video <- VideoRtb3Plc.fromRtb3(repr)
      } yield banner -> video
  }

  private def formatSize(size: (Int, Int), deviceType: DeviceType) = {
    val (width, height)       = size
    val portraitMobile        = (320, 480)
    val portraitTablet        = (768, 1024)
    val isPortraitOrientation = width < height

    if (deviceType.isDeviceTypeTablet) {
      if (isPortraitOrientation) portraitTablet else portraitTablet.swap
    } else {
      if (isPortraitOrientation) portraitMobile else portraitMobile.swap
    }
  }
}
