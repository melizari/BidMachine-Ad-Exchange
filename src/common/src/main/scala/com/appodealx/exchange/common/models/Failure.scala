package com.appodealx.exchange.common.models

case class Failure(reason: FailureReason, message: String) extends RuntimeException(message) {
  override def toString = {
    super.toString + s" with reason: $reason"
  }
}
