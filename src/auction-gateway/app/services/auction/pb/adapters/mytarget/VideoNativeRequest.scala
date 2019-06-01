package services.auction.pb.adapters.mytarget


import com.appodealx.openrtb.native.request._
import com.appodealx.openrtb.native.{DataType, ImageType, PlacementType, request}
import com.appodealx.openrtb.{Protocol, VideoLinearity}


object VideoNativeRequest {

  private val TITLE_ID = 1
  private val ICON_ID = 2
  private val IMAGE_ID = 3
  private val SPONSORED_ID = 4
  private val DESCRIPTION_ID = 5
  private val RATING_ID = 6
  private val LIKES_ID = 7
  private val DISPLAY_URL_ID = 8
  private val CTA_ID = 9
  private val VIDEO_ID = 10


  private val Mimes = List("video/mp4")

  private val Title = Asset(
    id = TITLE_ID,
    title = Some(request.Title(31))
  )

  private val Icon = Asset(
    id = ICON_ID,
    img = Some(Image(
      wmin = Some(50),
      `type` = Some(ImageType.Icon)
    ))
  )

  private val Main = Asset(
    id = IMAGE_ID,
    img = Some(Image(
      `type` = Some(ImageType.Main),
      wmin = Some(150)
    ))
  )

  private val SponsoredAsset = Asset(
    id = SPONSORED_ID,
    data = Some(Data(
      `type` = DataType.Sponsored
    ))
  )

  private val DescAsset = Asset(
    id = DESCRIPTION_ID,
    data = Some(Data(
      `type` = DataType.Desc
    ))
  )

  private val RatingAsset = Asset(
    id = RATING_ID,
    data = Some(Data(
      `type` = DataType.Rating
    ))
  )

  private val LikesAsset = Asset(
    id = LIKES_ID,
    data = Some(Data(
      `type` = DataType.Likes
    ))
  )

  private val DisplayUrlAsset = Asset(
    id = DISPLAY_URL_ID,
    data = Some(Data(
      `type` = DataType.DisplayUrl
    ))
  )

  private val CtaTextAsset = Asset(
    id = CTA_ID,
    data = Some(Data(
      `type` = DataType.CtaText
    ))
  )

  private val VideoAsset = Asset(
    id = VIDEO_ID,
    video = Some(Video(
      linearity = Some(VideoLinearity.Linear),
      mimes = Mimes,
      minduration = 15,
      maxduration = 30,
      protocols = List(Protocol.VAST_2)
    ))
  )

  private val Assets = List(Title, Icon, Main, SponsoredAsset, DescAsset, RatingAsset, LikesAsset, DisplayUrlAsset, CtaTextAsset, VideoAsset)

  val MyTargetDefault = Native(
    plcmttype = Some(PlacementType.PlacementType501),
    ver = Some("1.0"),
    assets = Assets
  )

}
