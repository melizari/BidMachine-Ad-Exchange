
import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.common.services.crypto.{JcaSigner, JcaSignerSettings}
import com.appodealx.exchange.druid.DruidService
import com.appodealx.exchange.settings.SettingsService
import com.lightbend.lagom.scaladsl.api.{LagomConfigComponent, ServiceInfo}
import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
import com.softwaremill.macwire.wire
import controllers._
import monix.execution.Scheduler
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DbName, SlickComponents}
import play.api.libs.ws.ahc.AhcWSComponents
import play.modules.swagger.SwaggerPluginImpl
import router.Routes
import utils.mail._

abstract class ApiGatewayComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with AssetsComponents
    with ApiGatewayHttpFiltersComponents
    with AhcWSComponents
    with LagomServiceClientComponents
    with LagomConfigComponent
    with EvolutionsComponents
    with SlickComponents
    with SlickEvolutionsComponents
    with SilhouetteComponents {

  val serviceInfo = ServiceInfo("api-gateway", Map.empty)

  applicationEvolutions

  override lazy val httpErrorHandler = wire[ErrorHandler]

  lazy val prefix = configuration.get[String]("prefix")
  override lazy val router = wire[Routes].withPrefix(prefix)
  lazy val jcaSignerSettings = JcaSignerSettings(configuration.get[String]("play.http.secret.key"))
  lazy val dbConfig = slickApi.dbConfig[PostgresProfile](DbName("default"))

  implicit val system = actorSystem
  implicit val scheduler = Scheduler(executionContext)

  lazy val resetPasswordLinkBuilder = wire[ResetPasswordLinkBuilder]
  lazy val jcaSigner = wire[JcaSigner]
  lazy val apiHelpController = wire[ApiHelpController]
  lazy val mandrillClient = wire[MandrillClient]
  lazy val mandrillMailer = wire[MandrillMailer]

  lazy val adminController = wire[AdminController]
  lazy val authController = wire[AuthController]
  lazy val homeController = wire[HomeController]
  lazy val userController = wire[UserController]
  lazy val sellersController = wire[SellersController]
  lazy val permissionsController = wire[PermissionsController]
  lazy val agencyController = wire[AgencyController]
  lazy val bidderController = wire[BidderController]
  lazy val adProfileController = wire[AdProfileController]

  lazy val adSpaceController = wire[AdSpaceController]
  val swaggerPlugin = wire[SwaggerPluginImpl]

//  lazy val settingsService = serviceClient.implement[SettingsService]
  lazy val settingsService = serviceClient.implement[SettingsService]
  lazy val druidService = serviceClient.implement[DruidService]

  lazy val analyticsController = wire[AnalyticsController]

  lazy val reportsController = wire[ReportsController]

}
