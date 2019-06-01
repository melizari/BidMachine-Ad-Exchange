package silhouette.persistence.tables

import com.appodealx.exchange.common.db.PostgresProfile.api._
import silhouette.{Permission, ResourceType}

class Permissions(tag: Tag) extends Table[Permission](tag, "permissions") {

  def userId = column[Long]("user_id")
  def resourceId = column[Long]("resource_id")
  def resourceType = column[ResourceType]("resource_type")

  def pkey = primaryKey("permissions_pkey", (userId, resourceType))
  def user = foreignKey("permissions_user_id_fkey", userId, Users)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (userId, resourceId, resourceType) <> ((Permission.apply _).tupled, Permission.unapply)

}

object Permissions extends TableQuery(new Permissions(_))