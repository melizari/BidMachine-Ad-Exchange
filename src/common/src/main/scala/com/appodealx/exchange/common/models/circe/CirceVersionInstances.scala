package com.appodealx.exchange.common.models.circe

import com.github.zafarkhaja.semver.Version
import io.circe.{Decoder, Encoder}

import scala.util.Try

trait CirceVersionInstances {

  implicit val versionEncoder = Encoder.encodeString.contramap[Version](_.toString)
  implicit val versionDecoder = Decoder.decodeString.emapTry(s => Try(Version.valueOf(s)))

}
