package com.appodealx.exchange.common.models.auction

import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.circe.CirceRtbInstances
import com.appodealx.exchange.common.models.jsoniter.JsoniterRtbInstances
import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.exchange.common.utils.TypeTag
import com.appodealx.exchange.common.utils.xtract.XmlReader
import com.appodealx.openrtb.native.response.Native
import io.circe.{Json, parser}
import play.twirl.api.Html

import cats.syntax.either._

trait Adm[A] extends TypeTag[A] { self =>

  def parse(adm: String): Either[Failure, A]

  def render(markup: A): Markup

  def is[AA: Adm] = Adm[AA] == self
}

object Adm {

  private def failure(s: String) = Failure(FailureReason.RtbAdmDecodingFailure, s)

  def apply[A: Adm] = implicitly[Adm[A]]

  implicit object MRAID extends Adm[Html] {
    def parse(adm: String) = Right(Html(adm))

    def render(markup: Html): Markup = HtmlMarkup(markup)
  }

  implicit object Vast extends Adm[VAST] {

    override def parse(adm: String) = parseXml(prepare(adm)).flatMap(decodeXml)

    override def render(markup: VAST): Markup = VastMarkup(markup)

    private def prepare(s: String) = s.replace("\\\"", "\"")

    private def parseXml(s: String) =
      Either
        .catchNonFatal(xml.XML.loadString(s))
        .leftMap(f => failure(f.getMessage))

    private def decodeXml(el: xml.Elem)(implicit r: XmlReader[VAST]) =
      r.read(el).fold(err => failure(err.toString).asLeft[VAST])(_.asRight[Failure])
  }

  implicit object NAST extends Adm[Native] with CirceRtbInstances with JsoniterRtbInstances {

    def parse(adm: String) = parseJson(adm).flatMap(decodeJson)

    def render(markup: Native): Markup = NativeMarkup(markup)

    private def parseJson(s: String) = parser.parse(s).leftMap(f => failure(f.message))

    private def decodeJson(json: Json) = json.as[Native].leftMap(f => failure(f.message))

  }
}
