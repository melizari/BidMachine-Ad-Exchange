package silhouette

import com.appodealx.exchange.settings.SettingsService
import com.mohiva.play.silhouette.api.{Authorization, Env}
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

trait SellerAuthorizations[E <: Env] extends Authorizations[E] {
  def settingsService: SettingsService

  case class WithSeller(sellerId: Long)(implicit ec: ExecutionContext)
    extends WithPermission(sellerId, ResourceType.Seller)

  case class WithSellerOfBannerAdSpace(adSpaceId: Long)(implicit ec: ExecutionContext) extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]): Future[Boolean] = {
      for {
        adSpace <- settingsService.readBannerAdSpaceById(adSpaceId).invoke()
        permission <- permissionsRepository.retrieve(identity.id.get, ResourceType.Seller)
        access = permission.exists(p => adSpace.exists(_.sellerId.contains(p.resourceId)))
      } yield access
    }
  }

  case class WithSellerOfVideoAdSpace(adSpaceId: Long)(implicit ec: ExecutionContext) extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]): Future[Boolean] = {
      for {
        adSpace <- settingsService.readVideoAdSpaceById(adSpaceId).invoke()
        permission <- permissionsRepository.retrieve(identity.id.get, ResourceType.Seller)
        access = permission.exists(p => adSpace.exists(_.sellerId.contains(p.resourceId)))
      } yield access
    }
  }

  case class WithSellerOfNativeAdSpace(adSpaceId: Long)(implicit ec: ExecutionContext) extends Authorization[User, E#A] {
    def isAuthorized[B](identity: User, authenticator: E#A)(implicit request: Request[B]): Future[Boolean] = {
      for {
        adSpace <- settingsService.readNativeAdSpaceById(adSpaceId).invoke()
        permission <- permissionsRepository.retrieve(identity.id.get, ResourceType.Seller)
        access = permission.exists(p => adSpace.exists(_.sellerId.contains(p.resourceId)))
      } yield access
    }
  }
}
