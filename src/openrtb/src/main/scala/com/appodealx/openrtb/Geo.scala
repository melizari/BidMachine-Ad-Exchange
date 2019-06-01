package com.appodealx.openrtb

case class Geo(lat: Option[Double] = None,
               lon: Option[Double] = None,
               `type`: Option[LocationType] = None,
               accuracy: Option[Int] = None,
               lastfix: Option[Int] = None,
               ipservice: Option[IpLocationService] = None,
               country: Option[String] = None,
               region: Option[String] = None,
               regionfips104: Option[String] = None,
               metro: Option[String] = None,
               city: Option[String] = None,
               zip: Option[String] = None,
               utcoffset: Option[Int] = None,
               ext: Option[Json] = None)

object Geo {
  implicit final class GeoOps(base: Geo) {
    def enrich(from: Geo) = {
      Geo(
        lat = base.lat.filter(_ != 0.0).orElse(from.lat),
        lon = base.lon.filter(_ != 0.0).orElse(from.lon),
        `type` = base.`type`.orElse(from.`type`),
        accuracy = base.accuracy.orElse(from.accuracy),
        lastfix = base.lastfix.orElse(from.lastfix),
        ipservice = base.ipservice.orElse(from.ipservice),
        country = base.country.filter(_.nonEmpty).orElse(from.country),
        region = base.region.filter(_.nonEmpty).orElse(from.region),
        regionfips104 = base.regionfips104.filter(_.nonEmpty).orElse(from.regionfips104),
        metro = base.metro.filter(_.nonEmpty).orElse(from.metro),
        city = base.city.filter(_.nonEmpty).orElse(from.city),
        zip = base.zip.filter(_.nonEmpty).orElse(from.zip),
        utcoffset = base.utcoffset.orElse(from.utcoffset),
        ext = base.ext.orElse(from.ext),
      )
    }
  }
}
