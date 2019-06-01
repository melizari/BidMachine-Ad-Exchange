package com.appodealx.exchange.common.models.rtb


import com.appodealx.exchange.common.utils.xtract._
import io.lemonlabs.uri.Uri
import org.joda.time.format._
import org.joda.time.{Duration => JodaDuration}
import play.api.libs.functional.syntax._

import scala.util.Try


package object vast {

  type Uri = com.appodealx.exchange.common.models.Uri

  private def formatBuilder = {
    new PeriodFormatterBuilder()
      .printZeroAlways()
      .minimumPrintedDigits(2)
      .appendHours()
      .appendSeparator(":")
      .appendMinutes()
      .appendSeparator(":")
      .appendSeconds()
  }

  private val durationFormatter = formatBuilder.toFormatter
  private val durationFormatterWithMillis = formatBuilder.appendSeparator(".").appendMillis3Digit().toFormatter

  private def safeParse(f: PeriodFormatter)(p: String): Option[JodaDuration] =
    Try(f.parsePeriod(p)).toOption.map(_.toStandardDuration)

  private def durationReader(f: PeriodFormatter) =
    __.read[String].collect(Function.unlift(safeParse(f)))

  implicit val jodaDurationXmlReader =
    durationReader(durationFormatter) or durationReader(durationFormatterWithMillis)

  implicit class DurationOps(d: JodaDuration) {
    def toFormattedString: String = durationFormatterWithMillis.print(d.toPeriod)
  }

  private def uriReader(s: String): Option[Uri] = Uri.parseOption(s)

  implicit val scalaURIXmlReader = __.read[String].collect(Function.unlift(uriReader))
}
