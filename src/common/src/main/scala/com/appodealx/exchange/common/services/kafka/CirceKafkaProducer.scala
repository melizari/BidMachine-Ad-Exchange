package com.appodealx.exchange.common.services.kafka

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import io.circe._
import io.circe.syntax._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}


class CirceKafkaProducer(kafkaProducer: KafkaProducer[String, Json]) {

  def send[V](topic: String, value: V)(implicit encoder: Encoder[V], ec: ExecutionContext): Future[Unit] = {
    kafkaProducer.send(KafkaProducerRecord(topic = topic, value = value.asJson)).map{ rm =>
      Logger.debug(s"CirceKafkaProducer: recordMessage: offset=${rm.offset()}, topic=${rm.topic()}, partition=${rm.partition()}, message=${value.asJson.pretty(Printer.noSpaces.copy(dropNullValues = true))}")
    }.recover { case e: Exception =>
      Logger.error(e.getMessage, e)
    }
  }

}