package com.appodealx.exchange.common.services.crypto


trait Signer {

  def sign(data: String): String

  def verify(data: String, signature: String): Boolean

}
