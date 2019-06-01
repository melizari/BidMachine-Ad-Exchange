package silhouette.repositories

import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.db.slick.HasDatabaseConfig
import silhouette.persistence.tables.Passwords
import slick.basic.DatabaseConfig

import scala.concurrent.ExecutionContext


class PasswordsRepository(protected val dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext)
  extends DelegableAuthInfoDAO[PasswordInfo]
    with HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax {

  import profile.api._

  private def loginInfoQuery(loginInfo: LoginInfo) = {
    Passwords.filter { password =>
      password.providerID === loginInfo.providerID &&
      password.providerKey === loginInfo.providerKey
    }
  }

  def find(loginInfo: LoginInfo) = {
    loginInfoQuery(loginInfo).map(_.passwordInfo).result.headOption.run()
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    (Passwords.returning(Passwords.map(_.passwordInfo)) += (loginInfo, authInfo)).run()
  }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    loginInfoQuery(loginInfo).update(loginInfo, authInfo).run().map(_ => authInfo)
  }

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
     Passwords
       .insertOrUpdate(loginInfo, authInfo)
       .map(_ => authInfo)
       .run()
  }

  def remove(loginInfo: LoginInfo) = {
    loginInfoQuery(loginInfo).delete.map(_ => ()).run()
  }
}
