package silhouette.persistence.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import com.mohiva.play.silhouette.api.LoginInfo

class UserLogins(tag: Tag) extends Table[(Long, LoginInfo)](tag, "user_logins") {

  def userId = column[Long]("user_id")
  def providerId = column[String]("login_provider_id")
  def providerKey = column[String]("login_provider_key")

  def loginInfo = (providerId, providerKey) <> ((LoginInfo.apply _).tupled, LoginInfo.unapply)

  def user = foreignKey("user_logins_user_id_fkey", userId, Users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def pkey = primaryKey("user_logins_pkey", (providerId, providerKey))

  def providerIdIdx = index("user_logins_provider_id_idx", (userId, providerId), unique = true)

  def * = (userId, loginInfo)
}

object UserLogins extends TableQuery(new UserLogins(_))
