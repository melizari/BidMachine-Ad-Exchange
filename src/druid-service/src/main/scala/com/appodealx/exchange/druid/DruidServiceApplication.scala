package com.appodealx.exchange.druid

import com.appodealx.exchange.druid.services.DruidBackendServiceImpl
import com.appodealx.exchange.settings.SettingsService
import com.lightbend.lagom.scaladsl.server.{ LagomApplication, LagomApplicationContext }
import com.softwaremill.macwire.wire
import play.api.libs.ws.ahc.AhcWSComponents
import scalacache.caffeine.CaffeineCache

abstract class DruidServiceApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with AhcWSComponents {

  lazy val localCache = CaffeineCache[Long]

  lazy val druidBackendService = wire[DruidBackendServiceImpl]

  lazy val settings = serviceClient.implement[SettingsService]

  override lazy val lagomServer = serverFor[DruidService](wire[DruidServiceImpl])
}
