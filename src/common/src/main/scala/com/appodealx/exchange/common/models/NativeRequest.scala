package com.appodealx.exchange.common.models

import com.appodealx.openrtb.Protocol
import com.appodealx.openrtb.native._
import com.appodealx.openrtb.native.request._

object NativeRequest {

  private val TITLE_ID = 123
  private val ICON_ID = 124
  private val IMAGE_ID = 128
  private val DESCRIPTION_ID = 127
  private val RATING_ID = 7
  private val CTA_ID = 8
  private val VIDEO_ID = 4


  private val Mimes = List("image/jpg","image/gif","image/png", "image/jpeg")

  private val Title = Asset(
    id = TITLE_ID,
    required = Some(true),
    title = Some(request.Title(40))
  )

  private val Icon = Asset(
    id = ICON_ID,
    required = Some(true),
    img = Some(Image(
      Some(ImageType.Icon),
      wmin = Some(50),
      hmin = Some(50),
      mimes = Some(Mimes)
    ))
  )

  private val Main = Asset(
    id = IMAGE_ID,
    required = Some(true),
    img = Some(Image(
      Some(ImageType.Main),
      wmin = Some(1200),
      hmin = Some(627),
      mimes = Some(Mimes)
    ))
  )

  private val VideoAsset = Asset(
    id = VIDEO_ID,
    video = Some(Video(
      mimes = List("video/mp4"),
      minduration = 1,
      maxduration = 90,
      protocols = List(Protocol.VAST_2, Protocol.VAST_2_WRAPPER)
    ))
  )

  private val Desc = Asset(id = DESCRIPTION_ID, required = Some(true), data = Some(Data(DataType.Desc)))
  private val Rating = Asset(id = RATING_ID, data = Some(Data(DataType.Rating)))
  private val Cta = Asset(id = CTA_ID, data = Some(Data(DataType.CtaText)))

  private val Assets = List(Title, Icon, Main, VideoAsset, Desc, Rating, Cta)

  val Default = Native(ver = Some("1.0"), assets = Assets)

}
