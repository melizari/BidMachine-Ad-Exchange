package com.appodealx.exchange.common.models.circe

import com.appodealx.exchange.common.models.analytics.{CallbackContext, DebugInfo, ErrorContext}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait CirceAnalyticsInstances extends CirceModelsInstances {

  implicit val contextCallbackDecoder = deriveDecoder[CallbackContext]
  implicit val contextCallbackEncoder = deriveEncoder[CallbackContext]

  implicit val debugInfoEncoder = deriveEncoder[DebugInfo]
  implicit val debugInfoDecoder = deriveDecoder[DebugInfo]

  implicit val vastErrorContextEncoder = deriveEncoder[ErrorContext]
  implicit val vastErrorContextDecoder = deriveDecoder[ErrorContext]

}
