package silhouette.repositories

import com.appodealx.exchange.settings.SettingsService
import com.appodealx.exchange.settings.models.seller.Seller
import utils.future._

import scala.concurrent.ExecutionContext

class SellersRepository(settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends ResourceRepository[Seller] {

  def retrieve(id: Long) = {
    settingsService
      .findSeller(id)
      .invoke()
      .mapComplete(_.toOption)
  }
}

