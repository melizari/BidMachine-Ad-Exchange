package services.settings

import com.appodealx.exchange.common.models.auction.Plc
import models.auction.AdRequest

trait AdNetworkSettings {

  def enabled[P: Plc](request: AdRequest[P]): Boolean

}

final class AlwaysEnabledNetwork extends AdNetworkSettings {
  override def enabled[P: Plc](request: AdRequest[P]) = true
}
