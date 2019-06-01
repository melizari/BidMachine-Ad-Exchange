import kamon.Kamon
import kamon.prometheus.PrometheusReporter
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future


trait KamonComponents {
  def applicationLifecycle: ApplicationLifecycle

  Kamon.addReporter(new PrometheusReporter())

  applicationLifecycle.addStopHook { () =>
    Future.successful(Kamon.stopAllReporters())
  }
}
