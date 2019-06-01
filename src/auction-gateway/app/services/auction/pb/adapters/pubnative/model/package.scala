package services.auction.pb.adapters.pubnative

import com.appodealx.openrtb.native.response._
import play.twirl.api.Xml

import cats.syntax.option._

package object model {

  val TITLE_ID       = 123
  val ICON_ID        = 124
  val IMAGE_ID       = 128
  val DESCRIPTION_ID = 127
  val RATING_ID      = 7
  val CTA_ID         = 8
  val VIDEO_ID       = 4

  implicit class PubNativeResponseOps(pnr: PubNativeResponse) {

    def nast: Native = {
      pnr.ads.headOption.map { ad =>
        val impTrackers = ad.beacons.filter(_.`type` == BeaconType.Impression).flatMap(_.data.url).some

        val link = Link(
          url = ad.link.getOrElse(
            throw new NoSuchElementException(
              "Convert PubNative response to nast failed: 'url' field in Link object is required but not provided."
            )
          ),
          clicktrackers = ad.beacons.filter(_.`type` == BeaconType.Click).flatMap(_.data.url).some,
          fallback = None,
          ext = None
        )

        val assets = ad.assets.map {
          case a if a.`type` == AssetType.Icon =>
            Asset(
              id = ICON_ID,
              required = true.some,
              img = a.data.url.map(
                pnIconUrl =>
                  Image(
                    url = pnIconUrl,
                    w = a.data.w,
                    h = a.data.h
                )
              )
            )
          case a if a.`type` == AssetType.Banner =>
            Asset(
              id = IMAGE_ID,
              required = true.some,
              img = a.data.url.map(
                pnBannerUrl =>
                  Image(
                    url = pnBannerUrl,
                    w = a.data.w,
                    h = a.data.h
                )
              )
            )
          case a if a.`type` == AssetType.Title =>
            Asset(
              id = TITLE_ID,
              required = true.some,
              title = a.data.text.map(
                text =>
                  Title(
                    text = text
                )
              )
            )
          case a if a.`type` == AssetType.Description =>
            Asset(
              id = DESCRIPTION_ID,
              required = true.some,
              data = a.data.text.map(text => Data(label = None, value = text))
            )
          case a if a.`type` == AssetType.Rating =>
            Asset(
              id = RATING_ID,
              data = a.data.number.map(num => Data(label = None, value = num.toString))
            )
          case a if a.`type` == AssetType.Cta =>
            Asset(
              id = CTA_ID,
              data = a.data.text.map(ctaText => Data(label = None, value = ctaText))
            )
        }

        Native(
          ver = "1.0".some,
          assets = assets,
          link = link,
          imptrackers = impTrackers,
          jstracker = None,
          ext = None
        )

      }

    } match {
      case Some(n) => n
      case None    => throw new NoSuchElementException(s"PubNativeResponse to NAST conversion failed")
    }

    def mraid: String =
      pnr.ads.headOption.flatMap { ad =>
        def html = ad.assets.find(_.`type` == AssetType.HtmlBanner).flatMap(_.data.html)

        def image = ad.assets.find(_.`type` == AssetType.StandardBanner).flatMap { a =>
          for {
            w   <- a.data.w
            h   <- a.data.h
            url <- a.data.url
          } yield
            s"""<a href="${ad.link}" target="_blank"><img width="$w" height="$h" style="border-style: none" src="$url" width="$w" height="$h"></a>"""

        }

        html orElse image
      } match {
        case Some(adm) => adm
        case None      => throw new NoSuchElementException("PubNative mraid creative constructs failed.")
      }

    def vast: Xml = {
      pnr.ads.headOption.flatMap { ad =>
        ad.assets.find(_.`type` == AssetType.Vast2).flatMap(pna => pna.data.vast2.map(Xml(_)))
      }
    } match {
      case Some(xml) => xml
      case None      => throw new NoSuchElementException("PubNative xml creative constructs failed.")
    }

  }

}
