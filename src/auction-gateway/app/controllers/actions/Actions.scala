package controllers.actions
import com.appodealx.exchange.common.services.GlobalConfigService
import monix.eval.Task
import monix.execution.Scheduler
import play.api.mvc._

import scala.concurrent.ExecutionContext

trait Actions { self: BaseController =>

  def globalConfig: GlobalConfigService[Task]

  def scheduler: Scheduler

  object NoFillFilter extends ActionFilter[Request] {
    override def executionContext: ExecutionContext = scheduler

    override def filter[A](request: Request[A]) =
      globalConfig.read
        .map(_.forceNoFill.getOrElse(false))
        .map(nf => Option(NoContent).filter(_ => nf))
        .runToFuture(scheduler)
  }

  val NoFillAction = Action andThen NoFillFilter

}
