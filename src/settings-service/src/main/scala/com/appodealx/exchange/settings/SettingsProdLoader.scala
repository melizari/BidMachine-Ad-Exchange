package com.appodealx.exchange.settings

import com.appodealx.lagom.dcos.CloudServiceLocatorComponents
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}

class SettingsProdLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication = new SettingsApplication(context)
    with CloudServiceLocatorComponents

  override def describeService: Option[Descriptor] = Some(readDescriptor[SettingsService])
}
