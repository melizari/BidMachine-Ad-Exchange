package com.appodealx.exchange.settings

import com.appodealx.exchange.common.db.PostgresProfile
import com.appodealx.exchange.common.services.SubstitutionService
import com.appodealx.exchange.settings.persistance.buyer.dao._
import com.appodealx.exchange.settings.persistance.seller.dao.{BannerAdSpaceDAO, NativeAdSpaceDAO, VideoAdSpaceDAO}
import com.appodealx.exchange.settings.persistance.seller.repos.SellerRepository
import com.appodealx.exchange.settings.services.{AgencyExternalServiceImpl, AgencySyncServiceImpl}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomServer}
import com.softwaremill.macwire.wire
import monix.execution.Scheduler
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.db.slick.{DbName, SlickComponents}
import play.api.libs.ws.ahc.AhcWSComponents

abstract class SettingsApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents
    with EvolutionsComponents
    with SlickComponents
    with SlickEvolutionsComponents{

  applicationEvolutions

  implicit val scheduler: Scheduler = Scheduler(executionContext)

  lazy val buyerDBConfig = slickApi.dbConfig[PostgresProfile](DbName("buyer"))
  lazy val sellerDBConfig = slickApi.dbConfig[PostgresProfile](DbName("seller"))

  lazy val bannerAdProfileDAO = new BannerAdProfileDAO(buyerDBConfig)
  lazy val videoAdProfileDAO = new VideoAdProfileDAO(buyerDBConfig)
  lazy val nativeAdProfileDAO = new NativeAdProfileDAO(buyerDBConfig)

  lazy val agencyDAO = new AgencyDAO(buyerDBConfig)
  lazy val bidderDAO = new BidderDAO(buyerDBConfig)

  lazy val bannerAdSpaceDAO = new BannerAdSpaceDAO(buyerDBConfig)
  lazy val videoAdSpaceDAO = new VideoAdSpaceDAO(buyerDBConfig)
  lazy val nativeAdSpaceDAO = new NativeAdSpaceDAO(buyerDBConfig)

  val substitutionService = wire[SubstitutionService]
  val externalAgencyService = wire[AgencyExternalServiceImpl]
  val syncService = wire[AgencySyncServiceImpl]

  lazy val sellerRepository = new SellerRepository(sellerDBConfig)

  override lazy val lagomServer: LagomServer = serverFor[SettingsService](wire[SettingsServiceImpl])
}
