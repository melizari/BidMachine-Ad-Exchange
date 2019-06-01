package com.appodealx.lagom.dcos

import java.net.URI

import com.lightbend.lagom.scaladsl.api.{Descriptor, ServiceLocator}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class K8sServiceLocator(config: Config) extends ServiceLocator {

  val namespace = config.getString("locator.kubernetes.namespace")
  val cluster = config.getString("locator.kubernetes.cluster")

  override def locate(name: String, serviceCall: Descriptor.Call[_, _]): Future[Option[URI]] = {
    Future.successful(Some(URI.create(s"http://$name.$namespace.$cluster:9000")))
  }

  override def doWithService[T](name: String, serviceCall: Descriptor.Call[_, _])(block: URI => Future[T])(implicit ec: ExecutionContext) =
    locate(name, serviceCall).flatMap {
      case Some(uri) => block(uri).map(Some.apply)
      case None      => Future.successful(None)
    }
}
