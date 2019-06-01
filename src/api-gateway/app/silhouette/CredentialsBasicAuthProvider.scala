package silhouette

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import scala.concurrent.ExecutionContext

class CredentialsBasicAuthProvider(override protected val authInfoRepository: AuthInfoRepository,
                                   override protected val passwordHasherRegistry: PasswordHasherRegistry)
                                  (override implicit val executionContext: ExecutionContext)
  extends BasicAuthProvider(authInfoRepository, passwordHasherRegistry){

  override def id = "credentials"
}
