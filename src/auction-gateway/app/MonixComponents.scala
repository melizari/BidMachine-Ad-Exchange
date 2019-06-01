import monix.execution.Scheduler
import play.api.BuiltInComponents

trait MonixComponents { self: BuiltInComponents =>

  implicit val scheduler = Scheduler(executionContext)

}
