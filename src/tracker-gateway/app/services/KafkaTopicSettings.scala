package services

import play.api.ConfigLoader.configLoader


case class KafkaTopicSettings(impressions: String,
                         clicks: String,
                         finish: String,
                         fills: String,
                         `custom-loaded-event`: String,
                         errors: String,
                         invalidEvents: String,
                         events: String)

object KafkaTopicSettings {
  implicit val kafkaSettingsLoader = configLoader.map { conf =>
    KafkaTopicSettings(
      impressions = conf.getString("impressions"),
      clicks = conf.getString("clicks"),
      finish = conf.getString("finish"),
      fills = conf.getString("fills"),
      `custom-loaded-event` = conf.getString("custom-loaded-event"),
      errors = conf.getString("errors"),
      invalidEvents = conf.getString("invalid"),
      events = conf.getString("events")
    )
  }
}