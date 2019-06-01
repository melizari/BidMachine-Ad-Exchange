package com.appodealx.exchange.druid.transport.models

import io.circe.generic.semiauto._
import io.swagger.annotations.ApiModel


@ApiModel
case class DictionaryItemDTO(value: String, label: String)

object DictionaryItemDTO {
  implicit val dictionaryItemDTODecoder = deriveDecoder[DictionaryItemDTO]
  implicit val dictionaryItemDTOEncoder = deriveEncoder[DictionaryItemDTO]
}