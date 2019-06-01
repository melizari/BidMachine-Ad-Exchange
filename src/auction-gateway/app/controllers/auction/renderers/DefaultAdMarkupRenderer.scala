package controllers.auction.renderers

import com.appodealx.exchange.common.models._
import io.circe.{Json, Printer}
import models.{Ad, DefaultWriteables}
import play.api.http.Writeable
import play.api.mvc.{Result, Results}
import DefaultHeaderRenderer._

object DefaultAdMarkupRenderer extends Results with DefaultWriteables {

  implicit val customPrinter = Printer.noSpaces.copy(dropNullValues = true)

  def renderAd(ad: Ad)(implicit w: Writeable[Json]): Result = {
    def render[M: Writeable](m: M) = Ok(m).withHeaders(renderHeaders(ad.metadata):_*)

    ad.markup match {
      case XmlMarkup(markup)    => render(markup)
      case HtmlMarkup(markup)   => render(markup)
      case VastMarkup(markup)   => render(markup)
      case NativeMarkup(markup) => render(markup)

      case PbMarkup(_) => NoContent
    }
  }
}
