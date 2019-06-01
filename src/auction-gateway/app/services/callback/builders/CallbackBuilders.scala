package services.callback.builders

import com.appodealx.exchange.common.models.analytics.TrackingExtendedEventType
import com.appodealx.exchange.common.services.ParamsSigner
import controllers.adtracker.routes
import io.bidmachine.protobuf.EventTypeExtended
import io.lemonlabs.uri.config.UriConfig
import play.api.Configuration

class ImpressionCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends EventCallbackBuilder(EventTypeExtended.EVENT_TYPE_EXTENDED_IMPRESSION, routes.EventTrackerController.event(), configuration)

class ClickCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends EventCallbackBuilder(EventTypeExtended.EVENT_TYPE_EXTENDED_CLICK, routes.EventTrackerController.event(), configuration)

class ClosedCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends EventCallbackBuilder(EventTypeExtended.EVENT_TYPE_EXTENDED_CLOSED, routes.EventTrackerController.event(), configuration)

class LoadedCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends EventCallbackBuilder(EventTypeExtended.EVENT_TYPE_EXTENDED_LOADED, routes.EventTrackerController.event(), configuration)

class CustomEventCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends EventCallbackBuilder(EventTypeExtended.Unrecognized(TrackingExtendedEventType.`CUSTOM_LOADED_EVENT`), routes.EventTrackerController.event(), configuration)

class DestroyedCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends EventCallbackBuilder(EventTypeExtended.EVENT_TYPE_EXTENDED_DESTROYED, routes.EventTrackerController.event(), configuration)

class ViewableCallbackBuilder(val paramsSigner: ParamsSigner, configuration: Configuration)(implicit uriConfig: UriConfig)
  extends EventCallbackBuilder(EventTypeExtended.EVENT_TYPE_EXTENDED_VIEWABLE, routes.EventTrackerController.event(), configuration)
