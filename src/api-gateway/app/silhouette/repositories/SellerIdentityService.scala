package silhouette.repositories

import silhouette.ResourceType

import scala.concurrent.ExecutionContext

class SellerIdentityService(users: UserIdentityService,
                            permissions: PermissionsRepository,
                            sellers: SellersRepository)(implicit ec: ExecutionContext)
  extends AccountIdentityService(users, permissions, sellers) {

  def resourceType = ResourceType.Seller
}
