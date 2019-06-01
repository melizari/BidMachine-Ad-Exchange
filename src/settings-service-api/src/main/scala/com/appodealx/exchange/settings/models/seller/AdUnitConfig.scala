package com.appodealx.exchange.settings.models.seller

import io.circe.Json

final case class AdUnitConfig(id: Option[Long],
                              eid: Option[String],
                              ks: Option[String],
                              appId: Option[Long],
                              demandPartnerCode: String,
                              adType: AdType,
                              format: Option[Vector[Format]],
                              isInterstitial: Option[Boolean],
                              isRewarded: Option[Boolean],
                              customParams: Option[Json])
