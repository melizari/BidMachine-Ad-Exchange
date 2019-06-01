package services.auction.rtb.reqmodifiers

import com.appodealx.exchange.common.models.jsoniter.JsoniterRtbInstances
import com.appodealx.exchange.common.utils.jsoniter.byteStringOfJson
import services.auction.pb.adapters.pubmatic.PubmaticSettings

object PubmaticModifier extends JsoniterRtbInstances {

  def apply(settings: PubmaticSettings): BidRequestModifier = (_, req) => {
      val app               = req.app
      val modifiedAppId     = app.flatMap(_.id).flatMap(id => settings.enabledApps.get(id))
      val modifiedPublisher = app.flatMap(_.publisher).map(_.copy(id = Some(settings.publisherId)))
      val modifiedApp       = app.map(_.copy(id = modifiedAppId, publisher = modifiedPublisher))

      val modifiedReq = req.copy(app = modifiedApp)

      (modifiedReq, byteStringOfJson(modifiedReq))
  }
}
