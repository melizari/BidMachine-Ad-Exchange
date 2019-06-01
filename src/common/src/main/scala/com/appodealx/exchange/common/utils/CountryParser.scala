package com.appodealx.exchange.common.utils

import com.neovisionaries.i18n.CountryCode

import scala.util.Try

object CountryParser {

//  val `ISO 3166-1 alpha-2` = 2
//  val `ISO 3166-1 alpha-3` = 3

  private def safe[A](a: => A): Option[A] = Try(a).toNotNullOption

  /**
    * Parse string to two digit country code
    * @param string string to parse
    * @return string in `ISO 3166-1 alpha-2` format. If not parsed then return "ZZ" default country
    */
  def parse(string: String): String = parseAlpha2(string).getOrElse("ZZ")

  private def parseCode(s: String) = {
    def fromString = safe(CountryCode.getByCode(s, false))
    def fromInt(code: Int) = safe(CountryCode.getByCode(code))

    fromString.orElse(safe(s.toInt).flatMap(fromInt))
  }

  def parseAlpha2(s: String): Option[String] = parseCode(s).map(_.getAlpha2)
  def parseAlpha3(s: String): Option[String] = parseCode(s).map(_.getAlpha3)

  def parseName(code: String): Option[String] = parseCode(code).map(_.getName)

  def parseCountryInstance(code:String): Option[CountryCode] = parseCode(code)

}
