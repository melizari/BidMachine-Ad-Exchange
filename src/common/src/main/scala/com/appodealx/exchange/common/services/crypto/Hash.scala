package com.appodealx.exchange.common.services.crypto

import java.security.MessageDigest

object Hash {

  def sha256(str: String): String = sha256(str.getBytes("UTF-8"))

  def sha256(bytes: Array[Byte]): String = {
    MessageDigest.getInstance("SHA-256").digest(bytes).map("%02x".format(_)).mkString
  }

}
