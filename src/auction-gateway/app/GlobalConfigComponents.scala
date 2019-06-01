import com.appodealx.exchange.common.models.dao.{GlobalConfigDAO, GlobalConfigDAOImpl}
import com.appodealx.exchange.common.services.GlobalConfigServiceImpl
import com.softwaremill.macwire.wire
import controllers.helpers.CustomControllerComponents
import monix.eval.Task
import monix.execution.Scheduler

trait GlobalConfigComponents {
  self: PostgresComponents
    with ScalaCacheComponents
    with RedisClientComponents =>

  import scalacache.Monix.modes._ //we need for GlobalConfigServiceImpl, IDEA doesn't know it yet.

  def customControllerComponents: CustomControllerComponents

  implicit def scheduler: Scheduler

  lazy val globalConfigService = wire[GlobalConfigServiceImpl[Task]]
  lazy val globalConfigDAO = wire[GlobalConfigDAOImpl[Task]]

}

