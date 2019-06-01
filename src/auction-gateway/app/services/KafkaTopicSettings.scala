package services

import play.api.ConfigLoader.configLoader


case class KafkaTopicSettings(bidRequests: String,
                              bids: String,
                              debugBidRequest: String,
                              adNetworkBids: String,
                              adNetworkRequest: String)

object KafkaTopicSettings {

  implicit val kafkaSettingsLoader = configLoader.map { conf =>
    KafkaTopicSettings(
      bidRequests = conf.getString("bidRequests"),
      bids = conf.getString("bids"),
      debugBidRequest = conf.getString("debugBidRequest"),
      adNetworkBids = conf.getString("adNetworkBids"),
      adNetworkRequest = conf.getString("adNetworkRequest")
    )
  }
}