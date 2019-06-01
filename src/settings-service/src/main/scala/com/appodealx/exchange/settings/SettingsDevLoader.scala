package com.appodealx.exchange.settings

import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}

class SettingsDevLoader extends LagomApplicationLoader{
  override def load(context: LagomApplicationContext): LagomApplication = new SettingsApplication(context)
  with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SettingsService])
}
