package services
import cats.MonadError
import cats.syntax.option._
import com.appodealx.exchange.common.db.typeclasses.Execute
import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.models.seller.Seller
import play.api.Logger
import scalacache.Mode
import scalacache.caffeine.CaffeineCache

import scala.concurrent.duration._
import scala.language.postfixOps

class SellerRepoImpl[F[_]: Execute: Mode](settingsService: SettingsService, cache: CaffeineCache[Seller])(
  implicit M: MonadError[F, Throwable]
) extends SellerRepo[F] {

  import cats.syntax.functor._
  import cats.syntax.applicativeError._

  val ttl = 30 minutes

  def findSeller(id: Long): F[Option[Seller]] =
    cache
      .cachingF(id)(ttl.some)(Execute[F].deferFuture(settingsService.findSeller(id).invoke()))
      .map(_.some)
      .handleError { e =>
        Logger.error("SellerService Error", e)
        None
      }

}
