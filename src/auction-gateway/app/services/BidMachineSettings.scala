package services

import com.appodealx.openrtb.CreativeAttribute
import play.api.ConfigLoader._


case class BidMachineSettings(expirationTime: Int,
                              viewabilityTimeThreshold: Int,
                              additionalInterstitialBlockedAttributes: List[CreativeAttribute],
                              additionalBannerBlockedAttributes: List[CreativeAttribute])

object BidMachineSettings {

  implicit val bmConfLoader = configLoader.map { conf =>
    BidMachineSettings(
      conf.getInt("expiration-time"),
      conf.getInt("viewability-time-threshold"),
      conf.getString("blocked-attributes.interstitial").split(",").map(_.trim).flatMap(s => CreativeAttribute.withValueOpt(s.toInt)).toList,
      conf.getString("blocked-attributes.banner").split(",").map(_.trim).flatMap(s => CreativeAttribute.withValueOpt(s.toInt)).toList
    )
  }
}
