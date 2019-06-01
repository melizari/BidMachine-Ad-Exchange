package models

import com.appodealx.exchange.common.models.{Failure, FailureReason, Platform}
import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.exchange.settings.models.seller.AdSpaceId
import com.appodealx.openrtb
import com.appodealx.openrtb._
import io.circe.Json

import scala.util.Try


case class TagRequest(`ad_space_id`: AdSpaceId,
                      `seller_id`: Long,

                      `bidfloor`: Double,
                      `test`: Option[Int],

                      `sdk`: Option[String],
                      `sdkver`: Option[String],

                       `regs_coppa`: Option[Int],
                       `regs_gdpr`: Option[Long],

                      `app_id`: Option[String],
                      `app_name`: Option[String],
                      `app_bundle`: Option[String],
                      `app_storeid`: Option[String],
                      `app_storeurl`: Option[String],
                      `app_policy`: Option[Int],
                      `app_paid`: Option[Int],
                      `app_publisher_id`: Option[String],
                      `app_publisher_name`: Option[String],
                      `app_cat`: Option[List[String]],
                      `app_ver`: Option[String],

                      `device_ifa`: Option[String],
                      `device_ip`: Option[String],
                      `device_ipv6`: Option[String],
                      `device_lang`: Option[String],
                      `device_hwv`: Option[String],
                      `device_ua`: Option[String],
                      `device_os`: Option[Platform],
                      `device_osv`: Option[String],
                      `device_h`: Option[Int],
                      `device_w`: Option[Int],
                      `device_type`: Option[DeviceType],
                      `device_conn_type`: Option[ConnectionType],
                      `device_make`: Option[String],
                      `device_model`: Option[String],
                      `device_carrier`: Option[String],
                      `device_lmt`: Option[Int],
                      `device_pxratio`: Option[Double],
                      `device_ppi`: Option[Int],

                      `geo_zip`: Option[String],
                      `geo_country`: Option[String],
                      `geo_city`: Option[String],
                      `geo_type`: Option[LocationType],
                      `geo_lon`: Option[Double],
                      `geo_lat`: Option[Double],
                      `geo_utcoffset`: Option[Int],

                       `user_gender`: Option[Gender],
                       `user_yob`: Option[Int],
                       `user_keywords`: Option[String],
                       `user_consent`: Option[String]) { dto =>

  def toRtbGeo = openrtb.Geo(
    lat = dto.`geo_lat`,
    lon = dto.`geo_lon`,
    `type` = dto.`geo_type`,
    country = dto.`geo_country`.flatMap(CountryParser.parseAlpha3),
    city = dto.`geo_city`,
    zip = dto.`geo_zip`,
    utcoffset = dto.`geo_utcoffset`
  )

  def toRtbUser = openrtb.User(
    yob = dto.`user_yob`.filterNot(_ == 0),
    gender = dto.`user_gender`,
    keywords = dto.`user_keywords`,
  )

  def toRtbDevice = openrtb.Device(
    ua = dto.`device_ua`,
    geo = Some(dto.toRtbGeo),
    lmt = dto.`device_lmt`.map(_ > 0),
    ip = dto.`device_ip`,
    ipv6 = dto.`device_ipv6`,
    devicetype = dto.`device_type`,
    make = dto.`device_make`,
    model = dto.`device_model`,
    os = dto.`device_os`.map(_.prettyValue).orElse(if (dto.`device_make`.contains("Apple")) Some(Platform.iOS.prettyValue) else None),
    osv = dto.`device_osv`,
    hwv = dto.`device_hwv`,
    h = dto.`device_h`,
    w = dto.`device_w`,
    ppi = dto.`device_ppi`,
    pxratio = dto.`device_pxratio`,
    language = dto.`device_lang`,
    carrier = dto.`device_carrier`,
    connectiontype = dto.`device_conn_type`,
    ifa = dto.`device_ifa`
  )

  def toRtbApp = Try(openrtb.App(
    id = dto.`app_id`,
    name = Some(dto.`app_name`.getOrElse(throw Failure(FailureReason.AppValidatingError, "No app name"))),
    bundle = Some(dto.`app_storeid`.getOrElse(throw Failure(FailureReason.AppValidatingError, "No app storeid"))),
    domain = None,
    storeurl = dto.`app_storeurl`,
    cat = dto.`app_cat`,
    sectioncat = None,
    pagecat = None,
    ver = dto.`app_ver`,
    privacypolicy = dto.`app_policy`.map(_ > 0),
    paid = dto.`app_paid`.map(_ > 0),
    publisher = Some(Publisher(name = Some(dto.`app_publisher_name`.getOrElse(throw Failure(FailureReason.AppValidatingError, "No app publisher name"))))),
    content = None,
    keywords = None,
    ext = Some(Json.obj("package_name" -> Json.fromString(dto.`app_bundle`.getOrElse(throw Failure(FailureReason.AppValidatingError, "No app bundle")))))
  )).toEither
}

object TagRequest {

  def fromMap(value: Map[String, String]) = TagRequest(
    `ad_space_id` = AdSpaceId(value("ad_space_id").toLong),
    `seller_id` = value("seller_id").toLong,

    `bidfloor` = value("bidfloor").toDouble,
    `test` = value.get("test").map(_.toInt),

    `sdk` = value.get("sdk"),
    `sdkver` = value.get("sdkver"),

    `regs_coppa` = value.get("regs_coppa").map(_.toInt),
    `regs_gdpr` = value.get("regs_gdpr").map(_.toInt),

    `app_id` = value.get("app_id"),
    `app_name` = value.get("app_name"),
    `app_bundle` = value.get("app_bundle"),
    `app_storeid` = value.get("app_storeid"),
    `app_storeurl` = value.get("app_storeurl"),
    `app_policy` = value.get("app_policy").map(_.toInt),
    `app_paid` = value.get("app_paid").map(_.toInt),
    `app_publisher_id` = value.get("app_publisher_id"),
    `app_publisher_name` = value.get("app_publisher_name"),
    `app_cat` = value.get("app_cat").map(_.split(",").toList),
    `app_ver` = value.get("app_ver"),

    `device_ifa` = value.get("device_ifa"),
    `device_ip` = value.get("device_ip"),
    `device_ipv6` = value.get("device_ipv6"),
    `device_lang` = value.get("device_lang"),
    `device_hwv` = value.get("device_hwv"),
    `device_ua` = value.get("device_ua"),
    `device_os` = value.get("device_os").flatMap(Platform.fromString),
    `device_osv` = value.get("device_osv"),
    `device_h` = value.get("device_h").map(_.toInt),
    `device_w` = value.get("device_w").map(_.toInt),
    `device_type` = value.get("device_type").flatMap(v => DeviceType.withValueOpt(v.toInt)),
    `device_conn_type` = value.get("device_conn_type").flatMap(v => ConnectionType.withValueOpt(v.toInt)),
    `device_make` = value.get("device_make"),
    `device_model` = value.get("device_model"),
    `device_carrier` = value.get("device_carrier"),
    `device_lmt` = value.get("device_lmt").map(_.toInt),
    `device_pxratio` = value.get("device_pxratio").map(_.toDouble),
    `device_ppi` = value.get("device_ppi").map(_.toInt),

    `geo_zip` = value.get("geo_zip"),
    `geo_country` = value.get("geo_country"),
    `geo_city` = value.get("geo_city"),
    `geo_type` = value.get("geo_type").flatMap(v => LocationType.withValueOpt(v.toInt)),
    `geo_lon` = value.get("geo_lon").map(_.toDouble),
    `geo_lat` = value.get("geo_lat").map(_.toDouble),
    `geo_utcoffset` = value.get("geo_utcoffset").map(_.toInt),

    `user_gender` = value.get("user_gender").flatMap(Gender.withValueOpt),
    `user_yob` = value.get("user_yob").map(_.toInt),
    `user_keywords` = value.get("user_keywords"),
    `user_consent` = value.get("user_consent")
  )
}