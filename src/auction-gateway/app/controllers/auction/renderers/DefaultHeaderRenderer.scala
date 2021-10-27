package controllers.auction.renderers

import models.auction.Metadata
import services.auction.Headers

object DefaultHeaderRenderer {

  def renderHeaders(m: Metadata): List[(String, String)] =
    if (m.renderMetadata) m.toList else List(Headers.`X-Appodeal-Ad-Type` -> m.`X-Appodeal-Ad-Type`, "Access-Control-Allow-Origin" -> "*")
}
