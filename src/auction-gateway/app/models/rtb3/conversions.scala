package models.rtb3

import com.appodealx.exchange.common.models.Platform
import com.appodealx.openrtb
import com.appodealx.openrtb.ConnectionType._
import com.appodealx.openrtb.DeviceType.{Mobile, Phone, Tablet}
import com.appodealx.openrtb.LocationType.{GPS, IP, UserProvided}
import com.appodealx.openrtb.{Gender, IpLocationService}
import io.bidmachine.protobuf.adcom.ConnectionType.{CONNECTION_TYPE_CELLULAR_NETWORK_2G, CONNECTION_TYPE_CELLULAR_NETWORK_3G, CONNECTION_TYPE_CELLULAR_NETWORK_4G, CONNECTION_TYPE_CELLULAR_NETWORK_UNKNOWN, CONNECTION_TYPE_ETHERNET, CONNECTION_TYPE_WIFI}
import io.bidmachine.protobuf.adcom.Context.App.Content.Producer
import io.bidmachine.protobuf.adcom.Context.{Data, Device, Geo, User}
import io.bidmachine.protobuf.adcom.DeviceType.{DEVICE_TYPE_MOBILE, DEVICE_TYPE_PHONE_DEVICE, DEVICE_TYPE_TABLET}
import io.bidmachine.protobuf.adcom.LocationType.{LOCATION_TYPE_GPS, LOCATION_TYPE_IP, LOCATION_TYPE_USER}
import io.bidmachine.protobuf.adcom.OS.{OS_ANDROID, OS_IOS}
import io.bidmachine.protobuf.adcom._
import io.circe.Json

import cats.syntax.option._

object conversions {

  implicit class RichAdComApp(app: Context.App) {

    def toRtb2App = openrtb.App(
      id = app.id.some,
      name = app.name.some,
      bundle = if (app.storeid.nonEmpty) app.storeid.some else app.bundle.some,
      domain = app.domain.some,
      storeurl = app.storeurl.some,
      cat = app.cat.toList.some,
      sectioncat = app.sectcat.toList.some,
      pagecat = app.pagecat.toList.some,
      ver = app.ver.some,
      privacypolicy = app.privpolicy.some,
      paid = app.paid.some,
      publisher = toRtbPublisher(app.pub),
      content = toRtbContent(app.content),
      keywords = app.keywords.some,
      ext = Json.obj("packagename" -> Json.fromString(app.bundle)).some
    )

    private def toRtbPublisher(optPub: Option[Context.App.Publisher]) =
      optPub match {
        case Some(p) =>
          Some(
            openrtb.Publisher(
              id = p.id.some,
              name = p.name.some,
              cat = p.cat.toList.some,
              domain = p.domain.some,
              ext = None
            )
          )
        case None => None
      }

    private def toRtbContent(optContext: Option[Context.App.Content]) =
      optContext match {
        case Some(c) =>
          Some(
            openrtb.Content(
              id = c.id.some,
              episode = c.episode.some,
              title = c.title.some,
              series = c.series.some,
              season = c.season.some,
              artist = c.artist.some,
              genre = c.genre.some,
              album = c.album.some,
              isrc = c.isrc.some,
              producer = toRtbProducer(c.producer),
              url = c.url.some,
              cat = c.cat.toList.some,
              prodq = toRtbQuality(c.prodq),
              context = toRtbContext(c.context),
              contentrating = c.rating.some,
              userrating = c.urating.some,
              qagmediarating = toRtbRating(c.mrating),
              keywords = c.keywords.some,
              livestream = c.live.some,
              sourcerelationship = c.srcrel.some,
              len = c.len.some,
              lanuage = c.lang.some,
              embeddable = c.embed.some,
              data = toRtbData(c.data.toList).some,
              ext = None
            )
          )
        case None => None
      }

    private def toRtbProducer(producer: Option[Producer]) = producer match {
      case Some(p) =>
        Some(
          openrtb.Producer(
            id = p.id.some,
            name = p.name.some,
            cat = p.cat.toList.some,
            domain = p.domain.some
          )
        )
      case None => None
    }

    private def toRtbQuality(prodq: ProductionQuality) =
      openrtb.ProductionQuality.values.find(pq => pq.value == prodq.value)

    private def toRtbContext(context: ContentContext) =
      openrtb.ContentContext.values.find(cc => cc.value == context.value)

    private def toRtbRating(mrating: MediaRating) =
      openrtb.QagMediaRating.values.find(qm => qm.value == mrating.value)

    private def toRtbData(adComData: List[Context.Data]) = {

      def toRtbSegment(adComSegment: List[Data.Segment]) = {

        def toOpenRtbSegment(s: Data.Segment) =
          openrtb.Segment(
            id = s.id.some,
            name = s.name.some,
            value = s.value.some
          )

        adComSegment map toOpenRtbSegment
      }

      def toOpenRtbData(adComData: Context.Data) =
        openrtb.Data(
          id = adComData.id.some,
          name = adComData.name.some,
          segment = toRtbSegment(adComData.segment.toList).some
        )

      adComData map toOpenRtbData
    }
  }

  implicit class RichAdComDevice(device: Device) {

    def toRtb2Device =
      openrtb.Device(
        h = device.h.some,
        w = device.w.some,
        os = osToPlatform(device.os),
        osv = device.osv.some,
        ua = device.ua.some,
        ip = device.ip.some,
        ipv6 = device.ipv6.some,
        geo = device.geo.map(toRtbGeo),
        lmt = device.lmt.some,
        hwv = device.hwv.some,
        ppi = device.ppi.some,
        ifa = device.ifa.some,
        make = device.make.some,
        model = device.model.some,
        pxratio = device.pxratio.toDouble.some,
        language = device.lang.some,
        carrier = device.carrier.some,
        devicetype = toRtbDeviceType(device.`type`),
        connectiontype = toRtbConnectionType(device.contype),
        ext = None
      )

    private def osToPlatform(os: OS): Option[String] = os match {
      case OS_IOS     => Platform.iOS.prettyValue.some
      case OS_ANDROID => Platform.Android.prettyValue.some
      case _          => None
    }

    private def toRtbDeviceType(dt: DeviceType): Option[openrtb.DeviceType] = dt match {
      case DEVICE_TYPE_MOBILE       => Mobile.some
      case DEVICE_TYPE_TABLET       => Tablet.some
      case DEVICE_TYPE_PHONE_DEVICE => Phone.some
      case _                        => None
    }

    private def toRtbConnectionType(ct: ConnectionType): Option[openrtb.ConnectionType] = ct match {
      case CONNECTION_TYPE_ETHERNET                 => Ethernet.some
      case CONNECTION_TYPE_WIFI                     => Wifi.some
      case CONNECTION_TYPE_CELLULAR_NETWORK_UNKNOWN => CellularUnknownGen.some
      case CONNECTION_TYPE_CELLULAR_NETWORK_2G      => Cellular2G.some
      case CONNECTION_TYPE_CELLULAR_NETWORK_3G      => Cellular3G.some
      case CONNECTION_TYPE_CELLULAR_NETWORK_4G      => Cellular4G.some
      case _                                        => None
    }
  }

  implicit class RichAdComUser(user: User) {

    def toRbt2User =
      openrtb.User(
        yob = user.yob.some.filterNot(_ == 0),
        gender = Gender.values.find(_.value == user.gender),
        keywords = user.keywords.some,
        geo = user.geo.map(toRtbGeo)
      )
  }

  private def toRtbGeo(g: Geo): openrtb.Geo =
    openrtb.Geo(
      lat = g.lat.toDouble.some,
      lon = g.lon.toDouble.some,
      `type` = toRtbGeoType(g.`type`),
      country = g.country.some,
      city = g.city.some,
      zip = g.zip.some,
      utcoffset = g.utcoffset.some,
      ipservice = IpLocationService.withValueOpt(g.ipserv.value)
    )

  private def toRtbGeoType(lt: LocationType): Option[openrtb.LocationType] = lt match {
    case LOCATION_TYPE_GPS  => GPS.some
    case LOCATION_TYPE_IP   => IP.some
    case LOCATION_TYPE_USER => UserProvided.some
    case _                  => None
  }
}
