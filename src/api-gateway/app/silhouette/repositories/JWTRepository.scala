package silhouette.repositories

import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import play.api.db.slick.HasDatabaseConfig
import silhouette.persistence.tables.JWTTokens
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

class JWTRepository(protected val dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext)
  extends AuthenticatorRepository[JWTAuthenticator]
  with HasDatabaseConfig[PostgresProfile]
  with DBIOActionSyntax {

  import profile.api._

  override def find(id: String): Future[Option[JWTAuthenticator]] = {
    JWTTokens.filter(_.id === id).result.headOption.run()
  }

  override def add(authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    (JWTTokens.returning(JWTTokens) += authenticator).run()
  }

  override def update(authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    JWTTokens
      .filter(_.id === authenticator.id)
      .update(authenticator)
      .map(_ => authenticator)
      .run()
  }

  override def remove(id: String): Future[Unit] = {
    JWTTokens.filter(_.id === id).delete.map(_ => ()).run()
  }
}
