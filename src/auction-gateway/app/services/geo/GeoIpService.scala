package services.geo

import java.net.InetAddress

import models.geo.GeoIpData

trait GeoIpService[F[_]] {

  def getGeoIpData(ip: InetAddress): F[Option[GeoIpData]]
}