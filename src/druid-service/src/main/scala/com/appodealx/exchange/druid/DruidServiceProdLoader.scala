package com.appodealx.exchange.druid

import com.appodealx.lagom.dcos.CloudServiceLocatorComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplicationContext, LagomApplicationLoader}

class DruidServiceProdLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext) =
    new DruidServiceApplication(context) with CloudServiceLocatorComponents

  override def describeService = Some(readDescriptor[DruidService])
}