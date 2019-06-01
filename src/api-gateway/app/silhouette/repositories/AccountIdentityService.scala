package silhouette.repositories

import cats.data.OptionT
import cats.instances.future._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import silhouette.{Account, ResourceType}

import scala.concurrent.ExecutionContext

abstract class AccountIdentityService[R](users: UserIdentityService,
                                         permissions: PermissionsRepository,
                                         resources: ResourceRepository[R])(implicit ec: ExecutionContext)
  extends IdentityService[Account[R]] {

  def resourceType: ResourceType

  def retrieve(loginInfo: LoginInfo) = {
    { for {
      user <- OptionT(users.retrieve(loginInfo))
      userId <- OptionT.fromOption(user.id)
      permission <- OptionT(permissions.retrieve(userId, resourceType))
      resource <- OptionT(resources.retrieve(permission.resourceId))
    } yield Account(user, resource) }.value
  }
}
