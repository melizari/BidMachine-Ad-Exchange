package services.settings.criteo
import java.util.concurrent.TimeUnit

import com.appodealx.exchange.common.models.auction.{Bidder, BidderId, Plc}
import com.typesafe.config.Config
import models.auction.AdRequest
import play.api.ConfigLoader

import scala.concurrent.duration.FiniteDuration

case class CriteoS2SSettings(zonesByBidder: Map[BidderId, Map[String, String]], ttl: FiniteDuration)

object CriteoS2SSettings {
  val ruId = BidderId(176L)
  val usId = BidderId(175L)

  implicit val configLoader: ConfigLoader[CriteoS2SSettings] = (rootConfig: Config, path: String) => {
    val config = rootConfig.getConfig(path)
    val sizes  = Set("320x50", "728x90", "320x480", "480x320", "768x1024", "1024x768")

    val ttl = FiniteDuration(config.getDuration("ttl").getSeconds, TimeUnit.SECONDS)

    val ruConfig = config.getConfig("s2s.ru")
    val ruZones  = sizes.map(s => s -> ruConfig.getString(s"zones.banner.$s")).toMap

    val naConfig = config.getConfig("s2s.na")
    val naZones  = sizes.map(s => s -> naConfig.getString(s"zones.banner.$s")).toMap

    val zonesByBidderId = Map(ruId -> ruZones, usId -> naZones)

    CriteoS2SSettings(zonesByBidderId, ttl)
  }

  implicit class S2SOps(settings: CriteoS2SSettings) {
    def getZoneId[P: Plc](req: AdRequest[P], bidder: Bidder): Option[String] =
      for {
        size   <- Plc[P].size(req.ad)
        id     <- bidder.id
        zones  <- settings.zonesByBidder.get(id)
        zoneId <- zones.get(size.width + "x" + size.height)
      } yield zoneId
  }
}
