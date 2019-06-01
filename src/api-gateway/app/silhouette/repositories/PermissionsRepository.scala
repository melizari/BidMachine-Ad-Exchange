package silhouette.repositories

import com.appodealx.exchange.common.db.{DBIOActionSyntax, PostgresProfile}
import play.api.db.slick.HasDatabaseConfig
import silhouette.persistence.tables.Permissions
import silhouette.{Permission, ResourceType}
import slick.basic.DatabaseConfig

import scala.concurrent.{ExecutionContext, Future}

class PermissionsRepository(protected val dbConfig: DatabaseConfig[PostgresProfile])(implicit ec: ExecutionContext)
  extends HasDatabaseConfig[PostgresProfile]
    with DBIOActionSyntax {

  import profile.api._

  def create(account: Permission): Future[Permission] = {
    (Permissions.returning(Permissions) += account).run()
  }

  def retrieve(userId: Long, resourceType: ResourceType): Future[Option[Permission]] = {
    Permissions
      .filter(acc => acc.userId === userId && acc.resourceType === resourceType)
      .result
      .headOption
      .run()
  }

  def delete(userId: Long, resourceType: ResourceType): Future[Boolean] = {
    Permissions
      .filter(p => p.userId === userId && p.resourceType === resourceType)
      .delete.run()
      .map(_ > 0)
  }

  def findWithResourceType(resourceType: ResourceType) = {
    Permissions
      .filter(p => p.resourceType === resourceType)
      .result
      .run()
  }

//  def check(userId: Long, subject: PermissionSubject): Future[Boolean] = {
//    Accounts.filter { perm =>
//      perm.userId === userId &&
//      perm.accountType === subject.subjectType &&
//      perm.entityId === subject.subjectId
//    }.length.result.map(_ > 0).run()
//  }

}
