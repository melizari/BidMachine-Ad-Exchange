package com.appodealx.exchange.common.models.auction

import play.api.libs.functional.syntax._
import play.api.libs.json.Writes
import play.twirl.api.BufferedContent


trait BufferedContentWrites {

  implicit def BufferedContentWrites[A <: BufferedContent[A]]: Writes[BufferedContent[A]] =
    Writes.StringWrites.contramap[BufferedContent[A]](_.body)

}
