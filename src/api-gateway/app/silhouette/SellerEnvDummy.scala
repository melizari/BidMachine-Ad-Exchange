package silhouette

import com.appodealx.exchange.settings.models.seller.Seller
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.DummyAuthenticator

trait SellerEnvDummy extends Env {
  type I = Account[Seller]
  type A = DummyAuthenticator
}
