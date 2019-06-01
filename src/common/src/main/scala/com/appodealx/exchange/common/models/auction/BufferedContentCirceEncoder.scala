package com.appodealx.exchange.common.models.auction

import io.circe.Encoder
import play.twirl.api.BufferedContent


trait BufferedContentCirceEncoder {
  implicit def bufferedContentCirceEncoder[A <: BufferedContent[A]]: Encoder[A] =
    Encoder.encodeString.contramap[A](_.body)


}