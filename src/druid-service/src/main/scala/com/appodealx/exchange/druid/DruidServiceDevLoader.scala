package com.appodealx.exchange.druid

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server.{LagomApplicationContext, LagomApplicationLoader}

class DruidServiceDevLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): DruidServiceApplication =
    new DruidServiceApplication(context) {
      override lazy val serviceLocator = NoServiceLocator
    }

  override def describeService = Some(readDescriptor[DruidService])
}

