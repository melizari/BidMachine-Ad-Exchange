package services.auction.pb.adapters.criteo.s2s.model.rq

import java.util.UUID

import com.appodealx.exchange.common.models.Platform.{Android, iOS}
import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.openrtb
import io.circe.derivation.deriveEncoder
import models.auction.AdRequest

private[s2s] case class BidRequest(requestid: String,
                                   auction: Auction,
                                   impressions: List[Impression],
                                   user: User,
                                   device: Device,
                                   publisher: Publisher,
                                   app: App,
                                   site: Option[Site] = None,
                                   badv: Option[List[String]],
                                   bcat: Option[List[String]] = None)

private[s2s] object BidRequest {
  implicit val encoder = deriveEncoder[BidRequest]

  def apply[P: Plc](ar: AdRequest[P], zoneId: String): Option[BidRequest] =
    for {
      device     <- device(ar.device)
      impression <- impression(ar, zoneId)
      publisher  <- publisher(ar)
      app        <- app(ar)
      u          = user(ar)
      id         = ar.id
      a          = auction(ar)
      badv       = ar.badv
    } yield
      BidRequest(
        requestid = id,
        auction = a,
        impressions = impression :: Nil,
        user = u,
        device = device,
        publisher = publisher,
        app = app,
        badv = badv
      )

  private def publisher[P: Plc](adRequest: AdRequest[P]) =
    for {
      id     <- adRequest.app.publisher.flatMap(_.id).orElse(adRequest.sellerId.map(_.toString))
      domain = adRequest.app.publisher.flatMap(_.domain)
    } yield Publisher(id, domain)

  private def auction[P: Plc](adRequest: AdRequest[P]) =
    Auction(id = Some(UUID.randomUUID.toString), timeout = adRequest.tmax, currency = Some("USD"))

  private def user[P: Plc](adRequest: AdRequest[P]) = {
    def getGeo(geo: Option[openrtb.Geo]) = geo map { g =>
      Geo(latitude = g.lat, longitude = g.lon)
    }

    User(
      id = adRequest.user.id,
      country = adRequest.device.geo.flatMap(_.country),
      coppa = adRequest.coppa,
      geo = getGeo(adRequest.device.geo),
      yob = adRequest.user.yob,
      gdpr = adRequest.gdpr,
      consent = adRequest.consent
    )
  }

  private def app[P: Plc](adRequest: AdRequest[P]) =
    for {
      bundle   <- adRequest.app.bundle
      id       = adRequest.app.id
      name     = adRequest.app.name
      domain   = adRequest.app.domain
      storeurl = adRequest.app.storeurl
    } yield App(id, name, bundle, domain, storeurl)

  private def impression[P: Plc](adRequest: AdRequest[P], zoneId: String) = {

    val P = Plc[P]

    for {
      size         <- P.size(adRequest.ad)
      tagid        <- adRequest.adSpaceId.map(_.value).orElse(adRequest.sellerId)
      id           = "1"
      tid          = adRequest.impId.getOrElse(adRequest.id)
      instl        = adRequest.interstitial
      sourcetype   = 3
      creativetype = P.name
      api          = P.apiFrameworks(adRequest.ad)
      sizes        = Some(List(Size(size.width, size.height)))
    } yield
      Impression(
        id = id,
        zoneid = zoneId,
        tid = tid,
        tagid = tagid.toString,
        sourcetype = sourcetype,
        creativetype = creativetype,
        displaytype = None,
        displaymanager = adRequest.sdk,
        displaymanagerver = adRequest.sdkVersion,
        instl = Some(instl),
        secure = Some(true),
        visibility = None,
        viewability = None,
        api = api,
        floorprice = adRequest.bidFloor,
        sizes = sizes,
        deals = Nil,
      )
  }

  private def device(d: openrtb.Device) = {

    def deviceCategory = d.os match {
      case Some(Android.prettyValue) => Some("GAID")
      case Some(iOS.prettyValue)     => Some("IDFA")
      case _                         => None
    }

    for {
      id             <- d.ifa
      ip             <- d.ip.orElse(d.ipv6)
      category       <- deviceCategory
      system         <- d.os
      carrier        = d.carrier
      connectiontype = d.connectiontype.map(_.value)
      lmt            = d.lmt
    } yield
      Device(id = id,
             ip = ip,
             category = category,
             system = system,
             carrier = carrier,
             connectiontype = connectiontype,
             lmt = lmt)
  }
}
