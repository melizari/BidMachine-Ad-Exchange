package services.auction.rtb3

import java.net.InetAddress

import com.appodealx.openrtb.{Device, Geo}
import services.geo.GeoIpServiceProxy
import services.geo.GeoUtils.{geoFromGeoIpData, getIp}

import cats.Applicative
import cats.syntax.option._
import cats.syntax.functor._
import cats.syntax.applicative._

object DeviceWithGeoEnricher {

  type IP = String

  def apply[F[_]: Applicative](geoServiceProxy: GeoIpServiceProxy[F]): (Device, IP) => F[Device] = (device, rawIp) => {
    val ip: Option[InetAddress] = getIp(device, rawIp)

    val geoFromService = ip match {
      case Some(i) => geoServiceProxy.getGeoIpData(i).map(_.map(geoFromGeoIpData))
      case _       => none[Geo].pure[F]
    }

    val geoFromDevice = device.geo

    geoFromService.map { geo =>
      //When both are present create new Geo with all defined fields from device's Geo
      //plus service's Geo fields which are not defined in device's Geo.
      (geoFromDevice, geo) match {

        case (Some(fromDevice), Some(fromService)) =>
          device.copy(geo = fromDevice.enrich(fromService).some, ip = ip.map(_.getHostAddress))

        case (g @ Some(_), None) => device.copy(geo = g, ip = ip.map(_.getHostAddress))
        case (_, g)              => device.copy(geo = g, ip = ip.map(_.getHostAddress))
      }
    }
  }
}
