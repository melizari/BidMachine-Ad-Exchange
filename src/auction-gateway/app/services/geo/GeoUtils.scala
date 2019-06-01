package services.geo
import java.net.InetAddress

import com.appodealx.openrtb.{Device, Geo, LocationType}
import models.geo.GeoIpData
import play.api.mvc.Headers

import scala.util.Try

object GeoUtils {

  def getIp(d: Device, ipFromHeaders: String): Option[InetAddress] = {

    val deviceIp = for {
      ip <- d.ip if ip.nonEmpty
      ia <- tryGetInetAddress(ip)
    } yield ia

    deviceIp orElse tryGetInetAddress(ipFromHeaders)
  }

  def ipFromHeaders(h: Headers): String =
    h.get("x-forwarded-for")
      .orElse(h.get("X-Forwarded-For"))
      .flatMap(_.split(",").headOption)
      .getOrElse("")

  def tryGetInetAddress(ip: String) =
    if (ip.nonEmpty)
      Try(InetAddress.getByName(ip)).toOption
    else None

  def geoFromGeoIpData(g: GeoIpData) =
    Geo(
      lat = g.lat,
      lon = g.lon,
      `type` = Some(LocationType.IP),
      ipservice = Some(g.ipLocationService),
      country = Some(g.country.getAlpha3),
      region = g.region,
      city = g.city,
      zip = g.zip
    )

}
