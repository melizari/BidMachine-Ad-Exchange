import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent, ServiceInfo}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.softwaremill.macwire.wire
import controllers._
import controllers.helpers.CustomControllerComponents
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.CustomPlayBodyParsers
import play.api.{BuiltInComponentsFromContext, NoHttpFiltersComponents}
import router.Routes

abstract class AuctionGatewayComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with NoHttpFiltersComponents
    with LagomServiceClientComponents
    with LagomConfigComponent
    with AhcWSComponents
    with PostgresComponents
    with ScalaCacheComponents
    with RedisClientComponents
    with KamonComponents
    with RepoComponents
    with GeoIpComponents
    with SharedComponents
    with AuctionComponents
    with LegacyAuctionComponents
    with GlobalConfigComponents
    with MonixComponents {

  val prefix = "/"

  override lazy val router = wire[Routes]

  def serviceInfo = ServiceInfo("auction-gateway", Map.empty)

  override lazy val httpErrorHandler = wire[CustomErrorHandler]

  lazy val customBodyParsers = CustomPlayBodyParsers(playBodyParsers)
  lazy val customControllerComponents = CustomControllerComponents(controllerComponents, customBodyParsers)

  lazy val healthCheckController = wire[HealthController]
}