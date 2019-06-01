package com.appodealx.exchange.common.services.kafka

import java.util

import io.circe._
import org.apache.kafka.common.serialization.{Serializer, StringSerializer}

class CirceSerializer extends Serializer[Json] {
  private val compactPrinter = Printer.noSpaces.copy(dropNullValues = true)
  private val stringSerializer = new StringSerializer

  def configure(configs: util.Map[String, _], isKey: Boolean): Unit = stringSerializer.configure(configs, isKey)

  def serialize(topic: String, data: Json): Array[Byte] = {
    val jsonString = data.pretty(compactPrinter)
    stringSerializer.serialize(topic, jsonString)
  }

  def close(): Unit = stringSerializer.close()
}

