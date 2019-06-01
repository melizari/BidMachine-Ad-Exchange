package services.callback.markup
import com.appodealx.exchange.common.models.analytics.CallbackContext
import com.appodealx.exchange.common.models.rtb.vast.VAST

package object video {

  type VastMarkupBuilder = (VAST, CallbackContext) => VAST
}
