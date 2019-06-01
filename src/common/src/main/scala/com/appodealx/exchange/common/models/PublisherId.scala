package com.appodealx.exchange.common.models

import slick.lifted.MappedTo

import scala.util.Try

case class PublisherId(value: Id) extends AnyVal with MappedTo[Id]

object PublisherId {
  def fromString(id: String)= Try(id.toLong).toOption.map(apply)
}