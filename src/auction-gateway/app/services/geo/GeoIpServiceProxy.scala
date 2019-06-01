package services.geo

import java.net.InetAddress
import models.geo.GeoIpData

trait GeoIpServiceProxy[F[_]] {

  def getGeoIpData(ip: InetAddress): F[Option[GeoIpData]]
}
