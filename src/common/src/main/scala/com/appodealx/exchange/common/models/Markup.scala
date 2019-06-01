package com.appodealx.exchange.common.models

import com.appodealx.exchange.common.models.rtb.vast.VAST
import com.appodealx.openrtb.native.response.Native
import io.circe.Json
import play.twirl.api.{Html, Xml}

sealed trait Markup

case class PbMarkup(markup: Json)       extends Markup
case class XmlMarkup(markup: Xml)       extends Markup
case class HtmlMarkup(markup: Html)     extends Markup
case class VastMarkup(markup: VAST)     extends Markup
case class NativeMarkup(markup: Native) extends Markup
