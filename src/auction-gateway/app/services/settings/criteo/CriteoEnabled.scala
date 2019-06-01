package services.settings.criteo
import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.exchange.common.models.dto.Banner
import models.auction.AdRequest
import services.settings.AdNetworkSettings

object CriteoEnabled {
  def apply(settings: CriteoEnabledSettings): AdNetworkSettings =
    new AdNetworkSettings {
      override def enabled[T: Plc](request: AdRequest[T]) = {
        val enabledForApp = request.app.bundle.exists(settings.enabledApps.contains)

        Plc[T].is[Banner] && enabledForApp
      }
    }
}
