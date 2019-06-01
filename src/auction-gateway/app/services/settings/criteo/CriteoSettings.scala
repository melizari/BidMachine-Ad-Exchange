package services.settings.criteo

import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import play.api.ConfigLoader
import services.settings.criteo
import services.settings.criteo.CriteoSettings.{AdSize, DeviceRegion, ZoneId}

import scala.concurrent.duration.FiniteDuration

case class CriteoSettings(profileId: String,
                          defaultEndpoint: String,
                          endpointsByRegions: Map[List[DeviceRegion], String],
                          qps: Int,
                          zonesByRegions: Map[List[DeviceRegion], Map[AdSize, ZoneId]],
                          ttl: FiniteDuration)

object CriteoSettings {

  type DeviceRegion = String
  type ZoneId = Int
  type AdSize = String

  implicit final class Ops(cs: CriteoSettings) {
    def getEndpointByRegion(region: DeviceRegion): Option[String] = {
      val key = cs.endpointsByRegions.keys.find(regions => regions.contains(region))

      key.flatMap(cs.endpointsByRegions.get)
    }

    def getZoneIdBy(region: DeviceRegion, size: String): Option[ZoneId] = {
      val key = cs.zonesByRegions.keys.find(regions => regions.contains(region)).getOrElse(List("default"))

      cs.zonesByRegions(key).get(size)
    }

    def getDefaultZoneId(size: String): Option[ZoneId] = {
      cs.zonesByRegions(List("default")).get(size)
    }
  }

  implicit val configLoader: ConfigLoader[CriteoSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    val sizes  = Set("320x50", "728x90", "320x480", "480x320", "768x1024", "1024x768")

    def zonesFrom(c: Config) = sizes.map(s => s -> c.getString(s).toInt).toMap

    val zones   = zonesFrom(config.getConfig("zones.banner"))
    val ruZones = zonesFrom(config.getConfig("zones.banner-ru"))
    val naZones = zonesFrom(config.getConfig("zones.banner-us"))

    val ttl = config.getDuration("ttl")

    val ruEndpoint = config.getString("ru-endpoint")
    val naEndpoint = config.getString("na-endpoint")
    val euEndpoint = config.getString("eu-endpoint")

    val ruRegions = config.getString("ru-region").split(",").toList
    val naRegions = config.getString("na-region").split(",").toList



    val endpointsByRegions      = Map(ruRegions -> ruEndpoint, naRegions -> naEndpoint)
    val zonesWithSizesByRegions = Map(ruRegions -> ruZones, naRegions -> naZones, List("default") -> zones)

    criteo.CriteoSettings(
      profileId = config.getString("profileid"),
      defaultEndpoint = euEndpoint,
      endpointsByRegions = endpointsByRegions,
      qps = config.getInt("qps"),
      zonesByRegions = zonesWithSizesByRegions,
      ttl = FiniteDuration(ttl.getSeconds, TimeUnit.SECONDS)
    )
  }
}
