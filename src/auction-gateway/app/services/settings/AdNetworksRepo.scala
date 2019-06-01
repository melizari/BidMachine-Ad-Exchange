package services.settings

import com.appodealx.exchange.common.models.auction.Plc
import models.auction.{AdRequest, AdUnit}

case class AdNetworksRepo(settings: List[(String, AdNetworkSettings)]) {

  def enabledAdNetworks[P: Plc](request: AdRequest[P]): List[AdUnit] = {
    settings.flatMap {
      case (name, s) if s.enabled(request) =>
        Some(
          AdUnit(sdk = name,
          sdkVer = "unknown",
          externalId = None,
          cpmEstimate = Some(request.bidFloor),
          customParams = None
        ))
      case _ => None
    }
  }
}
