package com.appodealx.exchange.common.models.circe

import io.circe.{Decoder, Encoder}
import slick.lifted.MappedTo

trait CirceMappedToInstances {

  implicit def mappedToEncoder[A, T](implicit ev: A <:< MappedTo[T], e: Encoder[T]): Encoder[A] = Encoder.instance(a => e(a.value))

  def mappedToDecoder[A <: MappedTo[T], T](b: T => A)(implicit d: Decoder[T]): Decoder[A] = d.map(b)

}
