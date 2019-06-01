package com.appodealx.lagom.dcos

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.softwaremill.macwire.wire
import com.typesafe.config._

trait CloudServiceLocatorComponents {
  def config: Config

  lazy val serviceLocators = Map(
    "marathon" -> wire[MarathonServiceLocator],
    "k8s"      -> wire[K8sServiceLocator]
  )

  lazy val serviceLocator: ServiceLocator = serviceLocators(config.getString("locator.name"))
}
