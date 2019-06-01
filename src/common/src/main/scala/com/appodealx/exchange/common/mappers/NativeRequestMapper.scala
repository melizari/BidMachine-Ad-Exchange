package com.appodealx.exchange.common.mappers

import cats.syntax.option._
import com.appodealx.openrtb
import com.appodealx.openrtb._
import com.appodealx.openrtb.native.{ContextSubtype, DataType, ImageType, PlacementType}
import com.appodealx.openrtb.native.request
import com.appodealx.openrtb.native.request._
import io.bidmachine.protobuf.adcom.Placement.DisplayPlacement.NativeFormat
import io.bidmachine.protobuf.adcom.Placement.DisplayPlacement.NativeFormat.AssetFormat
import io.bidmachine.protobuf.adcom.Placement.DisplayPlacement.NativeFormat.AssetFormat.{DataAssetFormat, ImageAssetFormat}
import io.bidmachine.protobuf.adcom.Placement.{DisplayPlacement, VideoPlacement}

object NativeRequestMapper {

  def toRtb2(dp: DisplayPlacement) = {

    def toNative(nf: NativeFormat) = {
      request.Native(
        assets = nf.asset.toList.map(toAssetVersion2),
        ver = "1.1".some,
        context = None,
        contentsubtype = ContextSubtype.withValueOpt(dp.context.value),
        plcmttype = PlacementType.withValueOpt(dp.ptype.value),
        plcmtcnt = None,
        seq = None
      )
    }

    dp.nativefmt.map(toNative)
  }

  private def toAssetVersion2(af: AssetFormat) = {
    Asset(
      id = af.id,
      required = af.req.some,
      title = af.title.flatMap(t => t.len.nonDefault).map(Title(_)),
      img = af.img.map(toImageVersion2),
      video = af.video.map(toVideoVersion2),
      data = af.data.map(toDataVersion2),
      ext = None
    )
  }

  private def toImageVersion2(iaf: ImageAssetFormat) = {
    Image(
      `type` = ImageType.values.find(i => i.value == iaf.`type`.value),
      w = iaf.w.nonDefault,
      wmin = iaf.wmin.nonDefault,
      h = iaf.h.nonDefault,
      hmin = iaf.hmin.nonDefault,
      mimes = iaf.mime.toList.some
    )
  }

  private def toVideoVersion2(vp: VideoPlacement) = {

    def toCompanionType = {
      val result = vp.comptype.toList.flatMap(c => CompanionType.withValueOpt(c.value))
      if (result.isEmpty) None else result.some
    }

    def toApiFramework = {
      val result = vp.api.toList.flatMap(a => ApiFramework.withValueOpt(a.value))
      if (result.isEmpty) None else result.some
    }

    request.Video(
      mimes = vp.mime.toList,
      minduration = vp.mindur,
      maxduration = vp.maxdur,
      protocols = vp.ctype.toList.flatMap(c => Protocol.withValueOpt(c.value)),
      w = vp.w.nonDefault,
      h = vp.h.nonDefault,
      startdelay = vp.delay.some,
      placement = openrtb.VideoPlacementType.withValueOpt(vp.ptype.value),
      linearity = VideoLinearity.withValueOpt(vp.linear.value),
      skip = vp.skip.some,
      skipmin = vp.skipmin.some,
      skipafter = vp.skipafter.some,
      battr = None,
      maxextended = vp.maxext.some,
      minbitrate = vp.minbitr.nonDefault,
      maxbitrate = vp.maxbitr.nonDefault,
      boxingallowed = vp.boxing.some,
      playbackmethod = PlaybackMethod.withValueOpt(vp.playmethod.value).map(List(_)),
      playbackend = openrtb.PlaybackCessationMode.withValueOpt(vp.playend.value),
      delivery = vp.delivery.toList.flatMap(d => ContentDeliveryMethod.withValueOpt(d.value)).some,
      pos = AdPosition.withValueOpt(vp.pos.value),
      companionad = None,
      api = toApiFramework,
      compaiontype = toCompanionType
    )
  }

  private def toDataVersion2(daf: DataAssetFormat) = {
    request.Data(
      `type` = DataType.withValue(daf.`type`.value),
      len = daf.len.nonDefault
    )
  }

  private implicit class FilterDefaultIntOps(i: Int) {
    def nonDefault = i.some.filter(_ != 0)
  }
}
