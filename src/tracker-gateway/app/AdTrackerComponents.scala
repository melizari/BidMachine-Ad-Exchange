import adtracker.Routes
import com.appodealx.exchange.common.models.SecureCallbackParams
import com.appodealx.exchange.common.services.ParamsSigner
import com.appodealx.exchange.common.services.crypto.{JcaSigner, JcaSignerSettings}
import com.appodealx.exchange.common.services.kafka.CirceKafkaProducer
import com.softwaremill.macwire._
import controllers.adtracker.{AdErrorTrackerController, AdTrackerController, EventTrackerController, HealthCheckController}
import monix.execution.Scheduler
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import play.api.{BuiltInComponentsFromContext, LoggerConfigurator, NoHttpFiltersComponents}
import services._


class AdTrackerComponents(context: Context)
  extends BuiltInComponentsFromContext(context)
    with AhcWSComponents
    with RedisClientComponents
    with NoHttpFiltersComponents
    with KafkaComponents {

  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment)
  }

  override def router: Router = {
    // add the prefix string in local scope for the Routes constructor
    val prefix: String = "/"
    wire[Routes]
  }

  implicit val scheduler = Scheduler(executionContext)

  lazy val datacenterSettings = configuration.get[DatacenterMetadataSettings]("settings.data-center-metadata")

  lazy val circeKafkaProducer: CirceKafkaProducer = wire[CirceKafkaProducer]

  lazy val jcaSignerSettings = JcaSignerSettings(configuration.get[String]("play.http.secret.key"))
  lazy val jcaSigner: JcaSigner = wire[JcaSigner]

  lazy val paramsSignService  = new ParamsSigner(jcaSigner, SecureCallbackParams.Names)

  lazy val emitterService = wire[EmitterService]

  lazy val adTrackerService = wire[AdTrackerService]

  lazy val adErrorTrackerService = wire[AdErrorTrackerService]

  lazy val adTrackerController = wire[AdTrackerController]

  lazy val adErrorTrackerController = wire[AdErrorTrackerController]

  lazy val eventTrackerController = wire[EventTrackerController]

  lazy val healthCheckController = wire[HealthCheckController]

  lazy val deDuplicatingService = wire[DeDuplicatingCheckerImpl]

  lazy val customFillTimeService = wire[CustomFillTimeImpl]

}