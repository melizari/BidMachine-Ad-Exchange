package models

import com.appodealx.exchange.common.models.Markup

case class PbAd(markup: Markup,
                impTrackers: List[String] = Nil,
                clickTrackers: List[String] = Nil)
