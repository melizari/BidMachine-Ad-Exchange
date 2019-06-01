package services.auction.pb.adapters.pubmatic

import com.appodealx.exchange.common.models.auction.Plc
import com.typesafe.config.Config
import models.auction.AdRequest
import play.api.{ConfigLoader, Configuration}
import services.settings.AdNetworkSettings

import scala.util.Try

final case class PubmaticSettings(enabledApps: Map[String, String], publisherId: String) extends AdNetworkSettings {

  override def enabled[P: Plc](request: AdRequest[P]) =  {
    request.app.id.exists(enabledApps.contains)
  }
}

object PubmaticSettings {

  implicit val configLoader: ConfigLoader[PubmaticSettings] = (rootConfig: Config, path: String) => {

    val conf = Configuration(rootConfig.getConfig(path))

    val enabledApps = conf
      .getOptional[String]("enabled-apps")
      .filter(_.nonEmpty)
      .flatMap(stringAppsPairsToMap)
      .getOrElse(Map.empty[String, String])

    val publisherId = conf.get[String]("publisher-id")

    PubmaticSettings(enabledApps, publisherId)
  }

  private def stringAppsPairsToMap(pairs: String) =
    Try {

      pairs
        .split(",")
        .toList
        .map(_.split(":"))
        .map(arr => arr(0) -> arr(1))
        .toMap

    }.toOption
}
