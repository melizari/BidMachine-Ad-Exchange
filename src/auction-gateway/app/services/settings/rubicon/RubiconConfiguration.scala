package services.settings.rubicon

import com.typesafe.config.Config
import play.api.ConfigLoader

import scala.collection.immutable.Set

final case class RubiconConfiguration(user: String,
                                      password: String,
                                      apps: Set[String],
                                      allowIpv6: Boolean,
                                      excludedCountries: Set[String],
                                      accountId: Int,
                                      siteId: Int,
                                      sizes: Set[String],
                                      zoneId: Int,
                                      zoneBySize: Map[String, Int],
                                      videoSizeId: Int)

object RubiconConfiguration {
  implicit val configLoader: ConfigLoader[RubiconConfiguration] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)

    val user     = config.getString("user")
    val password = config.getString("password")
    val sizes      = Set("320x480", "728x90", "300x250", "320x50", "480x320", "1024x768", "768x1024")
    val zoneBySize = sizes.map(size => size -> config.getInt(s"banner.$size")).toMap

    RubiconConfiguration(
      apps = config.getString("enabled-apps").split(",").toSet,
      allowIpv6 = config.getBoolean("allowed-ipv6"),
      user = user,
      password = password,
      //base64EncodedCredentials = base64EncodedCredentials,
      sizes = sizes,
      zoneId = config.getString("zone-id").toInt,
      zoneBySize = zoneBySize,
      videoSizeId = config.getInt("video.size-id"),
      excludedCountries = config.getString("countries-excluded").split(",").map(_.trim).toSet,
      accountId = config.getInt("account-id"),
      siteId = config.getString("site-id").toInt
    )
  }
}
