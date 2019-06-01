package silhouette

import com.appodealx.exchange.settings.models.seller.Seller
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator

trait SellerEnvBearer extends ResourceEnv {
  type R = Seller
  type A = BearerTokenAuthenticator
}
