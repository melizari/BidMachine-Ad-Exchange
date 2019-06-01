package silhouette.repositories

import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.db.slick.HasDatabaseConfig
import silhouette.persistence.tables.UserLogins
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

class UserLoginsRepository(protected val dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext)
    extends HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax {

  import profile.api._

  def create(userId: Long, loginInfo: LoginInfo): Future[LoginInfo] = {
    (UserLogins.returning(UserLogins.map(_.loginInfo)) += (userId, loginInfo)).run()
  }

  def retrieve(userId: Long, providerId: String): Future[Option[LoginInfo]] = {
    UserLogins
      .filter(login => login.userId === userId && login.providerId === providerId)
      .map(_.loginInfo)
      .result
      .headOption
      .run()
  }
}
