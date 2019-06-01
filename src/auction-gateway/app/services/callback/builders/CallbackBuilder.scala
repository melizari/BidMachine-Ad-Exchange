package services.callback.builders

import com.appodealx.exchange.common.models.analytics.CallbackContext
import com.appodealx.exchange.common.models.{CallbackTrackingMethod, Uri}
import com.appodealx.exchange.common.services.ParamsSigner
import models.RequestHost


trait CallbackBuilder {

  protected def paramsSigner: ParamsSigner

  def secure(context: CallbackContext): Boolean

  def build(context: CallbackContext,
            trackers: List[String],
            method: CallbackTrackingMethod,
            nurl: Option[String] = None,
            burl: Option[String] = None,
            escapeMacros: Boolean = false,
            metadata: Boolean = false)(implicit requestHost: RequestHost): Uri

  protected def scheme(context: CallbackContext): String = {
    if (secure(context)) "https" else "http"
  }
}
