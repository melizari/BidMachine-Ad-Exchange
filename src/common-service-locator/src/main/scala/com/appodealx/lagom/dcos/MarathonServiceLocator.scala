package com.appodealx.lagom.dcos

import java.net.URI

import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceLocator}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class MarathonServiceLocator(config: Config) extends ServiceLocator {
  private val app = config.getString("locator.marathon.app")
  private val env = config.getString("locator.marathon.env")

  override def locate(name: String, serviceCall: Descriptor.Call[_, _]): Future[Option[URI]] = {
    val serviceName = app + env + name
    Future.successful(Some(URI.create(s"http://$serviceName.marathon.l4lb.thisdcos.directory:9000")))
  }

  override def doWithService[T](name: String, serviceCall: Descriptor.Call[_, _])(block: URI => Future[T])(implicit ec: ExecutionContext): Future[Option[T]] =
    locate(name, serviceCall).flatMap {
      case Some(uri) => block(uri).map(Some.apply)
      case None      => Future.successful(None)
    }

}
