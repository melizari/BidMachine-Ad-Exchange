package services.callback.markup
import com.appodealx.exchange.common.models.analytics.CallbackContext

import scala.util.Try

package object banner {

  type HtmlMarkupBuilder = (String, CallbackContext) => String

  def versionSeq(string: String): List[Int] = string.split("\\.").map(str => Try(str.toInt).toOption.getOrElse(-1)).toList

}
