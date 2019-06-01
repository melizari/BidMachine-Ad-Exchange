package controllers.auction.renderers

import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.auction.Plc
import io.circe.{Json, Printer}
import models.{Ad, DefaultWriteables}
import play.api.http.Writeable
import play.api.libs.circe.Circe
import play.api.mvc.{Result, Results}

trait PbAdMarkupRendering extends DefaultWriteables with Results with Circe {

  private implicit val customPrinter = Printer.noSpaces.copy(dropNullValues = true)

  def renderAd[P: Plc](ad: Ad): Result = {
    def renderToJson[A: Writeable](m: A) = {
      val raw = implicitly[Writeable[A]].transform(m).utf8String

      val dimensions = ad.size.toList.flatMap {
        case Size(w, h) => List("w" -> Json.fromInt(w), "h" -> Json.fromInt(h))
      }

      val markup = "creative" -> Json.fromString(raw)

      Json.obj(dimensions :+ markup: _*)
    }

    val json = ad.markup match {
      case XmlMarkup(m)    => renderToJson(m)
      case HtmlMarkup(m)   => renderToJson(m)
      case VastMarkup(m)   => renderToJson(m)
      case NativeMarkup(m) => renderToJson(m)

      case PbMarkup(j) => j
    }

    Ok(json).withHeaders(DefaultHeaderRenderer.renderHeaders(ad.metadata): _*)
  }
}
