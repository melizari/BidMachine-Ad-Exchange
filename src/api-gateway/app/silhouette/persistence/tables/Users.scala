package silhouette.persistence.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import silhouette.{PersistentUser, UserRole}

class Users(tag: Tag) extends Table[PersistentUser](tag, "users") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def email = column[String]("email")
  def role = column[UserRole]("role")
  def name = column[Option[String]]("name")
  def company = column[Option[String]]("company")

  def * = (id.?, email, role, name, company) <> ((PersistentUser.apply _).tupled, PersistentUser.unapply)
}

object Users extends TableQuery(new Users(_))