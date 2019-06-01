package silhouette

import com.mohiva.play.silhouette.api.Authorization
import play.api.mvc.Request

import scala.concurrent.Future

trait ResourceAuthorizations[E <: ResourceEnv] {

  case class WithAccountRole(role: UserRole) extends Authorization[E#I, E#A] {
    override def isAuthorized[B](identity: E#I, authenticator: E#A)(implicit request: Request[B]) = {
      Future.successful(identity.user.role == role)
    }
  }

}
