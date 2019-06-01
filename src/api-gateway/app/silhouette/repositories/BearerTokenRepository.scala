package silhouette.repositories

import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import play.api.db.slick.HasDatabaseConfig
import silhouette.persistence.tables.BearerTokens
import slick.basic.DatabaseConfig

import scala.concurrent.ExecutionContext

class BearerTokenRepository(protected val dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext)
  extends AuthenticatorRepository[BearerTokenAuthenticator]
  with HasDatabaseConfig[PostgresProfile]
  with DBIOActionSyntax {

  import profile.api._

  def find(id: String) = {
    BearerTokens.filter(_.id === id).result.headOption.run()
  }

  def add(authenticator: BearerTokenAuthenticator) = {
    (BearerTokens.returning(BearerTokens) += authenticator).run()
  }

  def update(authenticator: BearerTokenAuthenticator) = {
    BearerTokens
      .filter(_.id === authenticator.id)
      .update(authenticator)
      .map(_ => authenticator)
      .run()
  }

  def remove(id: String) = {
    BearerTokens.filter(_.id === id).delete.map(_ => ()).run()
  }
}
