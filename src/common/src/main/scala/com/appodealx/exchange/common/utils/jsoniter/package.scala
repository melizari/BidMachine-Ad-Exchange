package com.appodealx.exchange.common.utils

import akka.util.ByteString
import com.github.plokhotnyuk.jsoniter_scala.core.{ writeToArray, JsonValueCodec }

package object jsoniter {

  def byteStringOfJson[T](t: T)(implicit codec: JsonValueCodec[T]) = ByteString(writeToArray[T](t))

}
