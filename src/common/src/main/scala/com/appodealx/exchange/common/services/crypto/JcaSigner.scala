package com.appodealx.exchange.common.services.crypto

import java.security.MessageDigest

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Hex


class JcaSigner(settings: JcaSignerSettings) extends Signer {

  override def sign(data: String): String = {
    val mac = Mac.getInstance("HmacSHA512")
    val secret = settings.secret
    val secretHash = MessageDigest.getInstance("SHA-256").digest(secret.getBytes("UTF-8"))
    mac.init(new SecretKeySpec(secretHash, "HmacSHA512"))
    val signature = Hex.encodeHexString(mac.doFinal(data.getBytes("UTF-8")).take(32)) // truncate sig to 32 bytes to match Kalium AuthenticationKey implementation

    signature
  }

  def verify(data: String, signature: String) = {
    val expectedSignature = sign(data)
    constantTimeEquals(expectedSignature, signature)
  }

  private def constantTimeEquals(a: String, b: String): Boolean = {
    if (a.length != b.length) {
      false
    } else {
      var equal = 0
      for (i <- 0 until a.length) {
        equal |= a(i) ^ b(i)
      }
      equal == 0
    }
  }
}