package silhouette.persistence.tables

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.appodealx.exchange.common.db.PostgresProfile.api._

class Passwords(tag: Tag) extends Table[(LoginInfo, PasswordInfo)](tag, "passwords") {

  def providerID = column[String]("provider_id")
  def providerKey = column[String]("provider_key")

  def loginInfo = (providerID, providerKey) <> (LoginInfo.tupled, LoginInfo.unapply)
  def loginInfoKey = primaryKey("passwords_pkey", (providerID, providerKey))

  def hasher = column[String]("hasher")
  def password = column[String]("password")
  def salt = column[Option[String]]("salt")

  def passwordInfo = (hasher, password, salt) <> (PasswordInfo.tupled, PasswordInfo.unapply)

  def * = (loginInfo, passwordInfo)

}

object Passwords extends TableQuery(new Passwords(_))