package com.appodealx.exchange.common.models

import scala.util.Try

case class Size(width: Int, height: Int)

object Size {
  val Mrec = Size(300, 250)

  private val sizePattern = """(\d+)[xX](\d+)""".r

  def of(size: String): Option[Size] = Try {
    size match {
      case sizePattern(w, h) => Size(w.toInt, h.toInt)
    }
  }.toOption
}
