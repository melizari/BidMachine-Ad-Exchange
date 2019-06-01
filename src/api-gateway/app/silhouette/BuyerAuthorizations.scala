package silhouette

import com.appodealx.exchange.settings.SettingsService
import com.mohiva.play.silhouette.api.{Authorization, Env}
import play.api.mvc.Request

import scala.concurrent.ExecutionContext

trait BuyerAuthorizations[E <: Env] extends Authorizations[E] {
  def settingsService: SettingsService

  implicit class UserAccess(user: User) {
    def canAccessBidder(bidderId: Long)(implicit ec: ExecutionContext) = {
      for {
        bidder <- settingsService.readBidder(bidderId).invoke()
        permission <- permissionsRepository.retrieve(user.id.get, ResourceType.Agency)
        access = permission.exists(_.resourceId == bidder.agencyId.value)
      } yield access
    }
  }

  case class WithAgency(agencyId: Long)(implicit ec: ExecutionContext)
    extends WithPermission(agencyId, ResourceType.Agency)

  case class WithBidder(bidderId: Long)(implicit ec: ExecutionContext) extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]) = {
      identity canAccessBidder bidderId
    }
  }

  case class WithBannerAdProfile(profileId: Long)(implicit ec: ExecutionContext)
    extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]) = {
      for {
        profile <- settingsService.readBannerAdProfile(profileId).invoke()
        access <- identity canAccessBidder profile.bidderId.value
      } yield access
    }
  }

  case class WithVideoAdProfile(profileId: Long)(implicit ec: ExecutionContext)
    extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]) = {
      for {
        profile <- settingsService.readVideoAdProfile(profileId).invoke()
        access <- identity canAccessBidder profile.bidderId.value
      } yield access
    }
  }

  case class WithNativeAdProfile(profileId: Long)(implicit ec: ExecutionContext)
    extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]) = {
      for {
        profile <- settingsService.readNativeAdProfile(profileId).invoke()
        access <- identity canAccessBidder profile.bidderId.value
      } yield access
    }
  }

}
