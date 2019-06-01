import com.lightbend.lagom.scaladsl.api.LagomConfigComponent
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.softwaremill.macwire.wire
import monix.eval.Task
import play.api.BuiltInComponentsFromContext
import services.geo._
import services.settings.{DataCenterEndpointsSettings, GeoIpSettings, MaxMindLocalSettings}

trait GeoIpComponents {
  self: BuiltInComponentsFromContext with LagomServiceClientComponents with LagomConfigComponent =>


  lazy val maxMindLocalService = wire[MaxMindLocalService]

  private lazy val geoServices: List[GeoIpService[Task]] = List(maxMindLocalService)

  lazy val dcEndpointSettings = configuration.get[DataCenterEndpointsSettings]("settings.data-center-metadata")
  lazy val geoIpSettings      = configuration.get[GeoIpSettings]("settings.geoip")
  lazy val maxMindSettings    = configuration.get[MaxMindLocalSettings]("settings.geoip.maxmind")

  lazy val geoIpServiceProxyImp = wire[GeoIpServiceProxyImp[Task]]

}
