package services.callback.builders

import com.appodealx.exchange.common.services.ParamsSigner
import controllers.adtracker.routes
import io.lemonlabs.uri.config.UriConfig
import play.api.Configuration


class LegacyImpressionCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends LegacyCallbackBuilder(routes.AdTrackerController.impressionTyped, configuration)

class LegacyClickCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends LegacyCallbackBuilder(routes.AdTrackerController.clickTyped, configuration)

class LegacyFinishCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends LegacyCallbackBuilder(routes.AdTrackerController.finishTyped, configuration)

class LegacyFillsCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends LegacyCallbackBuilder(routes.AdTrackerController.fillTyped, configuration)

class CustomEventLegacyCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends LegacyCallbackBuilder(routes.AdTrackerController.customEventTyped, configuration)
