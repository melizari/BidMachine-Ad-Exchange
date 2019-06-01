package com.appodealx.openrtb.native.response

import cats.syntax.option._
import com.appodealx.openrtb.{Json, Url}
import io.bidmachine.protobuf.adcom.Ad.Display.Native.Asset.{DataAsset, ImageAsset, TitleAsset, VideoAsset}
import io.bidmachine.protobuf.adcom.Ad.Display.Native.LinkAsset


case class Native(ver: Option[String],
                  assets: List[Asset],
                  link: Link,
                  imptrackers: Option[List[Url]],
                  jstracker: Option[String],
                  ext: Option[Json])
object Native {

  implicit class ToVersion3(native: Native) {

    def toVersion3 = {
      io.bidmachine.protobuf.adcom.Ad.Display.Native(
        link = toVersion3LinkAsset(native.link).some,
        asset = native.assets.map(toVersion3Asset)
      )
    }

    private def toVersion3LinkAsset(link: Link): LinkAsset = {
      LinkAsset(
        url = link.url,
        urlfb = link.fallback.getOrElse(""),
        trkr = link.clicktrackers.getOrElse(Nil)
      )
    }

    private def toVersion3Asset(asset: Asset): io.bidmachine.protobuf.adcom.Ad.Display.Native.Asset = {
      io.bidmachine.protobuf.adcom.Ad.Display.Native.Asset(
        id = asset.id,
        req = asset.required.getOrElse(false),
        title = asset.title.map(toVersion3Title),
        image = asset.img.map(toVersion3Image),
        video = asset.video.map(toVersion3Video),
        data = asset.data.map(toVersion3Data),
        link = asset.link.map(toVersion3LinkAsset)
      )
    }

    private def toVersion3Title(title: Title) = {
      TitleAsset(
        text = title.text,
        len = title.text.length
      )
    }

    private def toVersion3Image(image: Image) = {
      ImageAsset(
        url = image.url,
        w = image.w.getOrElse(0),
        h = image.h.getOrElse(0),
      )
    }

    private def toVersion3Video(video: Video) = {
      VideoAsset(
        adm = video.vasttag
      )
    }

    private def toVersion3Data(data: Data) = {
      DataAsset(
        value = data.value,
        len = data.value.length
      )
    }
  }
}
