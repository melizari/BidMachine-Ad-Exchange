package silhouette

import com.mohiva.play.silhouette.api.{Authorization, Env}
import play.api.mvc.Request
import silhouette.repositories.PermissionsRepository

import scala.concurrent.{ExecutionContext, Future}

trait Authorizations[E <: Env] {
  def permissionsRepository: PermissionsRepository

  case class WithRole(role: UserRole) extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]) = {
      Future.successful(identity.role == role)
    }
  }

  object WithRoleAdmin extends WithRole(UserRole.Admin)

  class WithPermission(resourceId: Long, resourceType: ResourceType)(implicit ec: ExecutionContext) extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]) = {
      permissionsRepository
        .retrieve(identity.id.get, resourceType)
        .map(_.exists(_.resourceId == resourceId))
    }
  }

}


