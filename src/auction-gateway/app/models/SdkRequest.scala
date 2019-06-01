package models

import cats.data.NonEmptyList
import com.appodealx.exchange.common.models.Platform
import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.exchange.settings.models.seller.AdSpaceId
import com.appodealx.openrtb
import com.appodealx.openrtb._
import io.circe.Json
import io.circe.syntax._
import io.circe.parser._

// Snake case for auto JSON decoder generation
case class SdkRequest(`external_app_id`: Option[String],
                      `ad_space_id`: AdSpaceId,
                      `publisher_id`: Long,
                      impid: Option[String],
                      bidfloors: List[Double],
                      test: Option[Int],
                      coppa: Option[Int],
                      `native_ad_type`: Option[String],
                      ifa: Option[String],
                      ip: Option[String],
                      ipv6: Option[String],
                      ver: Option[String],
                      language: Option[String],
                      hwv: Option[String],
                      ua: Option[String],
                      os: Option[Platform],
                      osv: Option[String],
                      h: Option[Int],
                      w: Option[Int],
                      devicetype: Option[DeviceType],
                      connectiontype: Option[ConnectionType],
                      make: Option[String],
                      model: Option[String],
                      carrier: Option[String],
                      lmt: Option[Int],
                      pxratio: Option[Double],
                      ppi: Option[Int],
                      zip: Option[String],
                      country: Option[String],
                      city: Option[String],
                      `geo_type`: Option[LocationType],
                      lon: Option[Double],
                      lat: Option[Double],
                      utcoffset: Option[Int],
                      gender: Option[Gender],
                      yob: Option[Int],
                      keywords: Option[String],
                      `device_ext`: Option[Json],
                      `app_ext`: Option[Json],
                      `dm_ver`: Option[String],
                      `e_ci_id`: Option[Long],
                      `metadata_headers`: Option[Int],
                      // GDPR
                      gdpr: Option[Long],
                      consent: Option[String],
                      marketplaces: Option[NonEmptyList[MarketplaceType]]) { dto =>

  def toRtbGeo = openrtb.Geo(
    lat = dto.lat,
    lon = dto.lon,
    `type` = dto.`geo_type`,
    country = dto.country.flatMap(CountryParser.parseAlpha3).orElse(Some("ZZZ")),
    city = dto.city,
    zip = dto.zip,
    utcoffset = dto.utcoffset
  )

  def toRtbUser = openrtb.User(
    yob = dto.yob.filterNot(_ == 0),
    gender = dto.gender,
    keywords = dto.keywords,
    ext = dto.consent.map(c => Json.obj("consent" := c))
  )

  def toRtbDevice = openrtb.Device(
    ua = dto.ua,
    geo = Some(dto.toRtbGeo),
    lmt = dto.lmt.map(_ > 0),
    ip = dto.ip,
    ipv6 = dto.ipv6,
    devicetype = dto.devicetype,
    make = dto.make,
    model = dto.model,
    os = dto.os.map(_.prettyValue).orElse(if (dto.make.contains("Apple")) Some(Platform.iOS.prettyValue) else None),
    osv = dto.osv,
    hwv = dto.hwv,
    h = dto.h,
    w = dto.w,
    ppi = dto.ppi,
    pxratio = dto.pxratio,
    language = dto.language,
    carrier = dto.carrier,
    connectiontype = dto.connectiontype,
    ifa = dto.ifa,
    ext = dto.`device_ext`
  )

}

object SdkRequest {

  import conversions.ConnectionTypeHelper
  import cats.syntax.list._

  def fromMap(value: Map[String, String]) = {
    val deviceType = value.get("devicetype").flatMap(v => DeviceType.withValueOpt(v.toInt))
    val connType   = value.get("connectiontype").map(ConnectionType.fromString)
    val locType    = value.get("geo_type").flatMap(v => LocationType.withValueOpt(v.toInt))

    val bidfloor  = value.get("bidfloor").map(_.toDouble).toList
    val bidfloors = value.get("pricefloors").map(_.split(',').toList.map(_.toDouble)).toList.flatten

    val marketPlaces = value
      .get("marketplaces")
      .map(_.split(',').toList)
      .map(l => l.flatMap(MarketplaceType.withValueOpt))
      .flatMap(_.toNel)

    SdkRequest(
      `external_app_id` = value.get("external_app_id"),
      `ad_space_id` = AdSpaceId(value("ad_space_id").toLong),
      `publisher_id` = value("publisher_id").toLong,
      impid = value.get("impid"),
      bidfloors = bidfloor ++ bidfloors,
      test = value.get("test").map(_.toInt),
      coppa = value.get("coppa").map(_.toInt),
      `native_ad_type` = value.get("native_ad_type"),
      ifa = value.get("ifa"),
      ip = value.get("ip"),
      ipv6 = value.get("ipv6"),
      ver = value.get("ver"),
      language = value.get("language"),
      hwv = value.get("hwv"),
      ua = value.get("ua"),
      os = value.get("os").flatMap(Platform.fromString),
      osv = value.get("osv"),
      h = value.get("h").map(_.toInt),
      w = value.get("w").map(_.toInt),
      devicetype = deviceType,
      connectiontype = connType,
      make = value.get("make"),
      model = value.get("model"),
      carrier = value.get("carrier"),
      lmt = value.get("lmt").map(_.toInt),
      pxratio = value.get("pxratio").map(_.toDouble),
      ppi = value.get("ppi").map(_.toInt),
      zip = value.get("zip"),
      country = value.get("country"),
      city = value.get("city"),
      `geo_type` = locType,
      lon = value.get("lon").map(_.toDouble),
      lat = value.get("lat").map(_.toDouble),
      utcoffset = value.get("utcoffset").map(_.toInt),
      gender = value.get("gender").flatMap(Gender.withValueOpt),
      yob = value.get("yob").map(_.toInt),
      keywords = value.get("keywords"),
      `device_ext` = value.get("device_ext").flatMap(parse(_).toOption),
      `app_ext` = value.get("app_ext").flatMap(parse(_).toOption),
      `dm_ver` = value.get("dm_ver"),
      `e_ci_id` = value.get("e_ci_id").map(_.toInt),
      `metadata_headers` = value.get("metadata_headers").map(_.toInt),
      gdpr = value.get("gdpr").map(_.toInt),
      consent = value.get("consent"),
      marketplaces = marketPlaces
    )
  }
}
