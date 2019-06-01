package utils.mail

import com.appodealx.exchange.common.services.crypto.Signer
import play.api.Configuration
import play.api.mvc.{Call, RequestHeader}

class ResetPasswordLinkBuilder(signer: Signer, configuration: Configuration) {

  val pathSetPassword = configuration.get[String]("pathRenewPassword")

  private def stringToSign(email: String, expiresAt: Long) = {
    s"email=$email&exp=$expiresAt"
  }

  def verify(email: String, expiresAt: Long, signature: String) = {
    signer.verify(stringToSign(email, expiresAt), signature)
  }

  def getLink(email: String, expirationTimeMillis: Long)(implicit request: RequestHeader) = {
    val s = stringToSign(email, expirationTimeMillis)
    val linkParameters: String = s"?$s&signature=${signer.sign(s)}"
    Call("GET", pathSetPassword + linkParameters).absoluteURL()
  }

}
