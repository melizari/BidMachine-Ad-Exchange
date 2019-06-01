package com.appodealx.exchange.common

import java.net._
import java.nio.charset.StandardCharsets
import java.util.{Base64, UUID}

import com.appodealx.openrtb.CreativeAttribute
import io.bidmachine.protobuf.adcom
import io.lemonlabs.uri.{Uri => LemonUri}
import akka.http.scaladsl.model.Uri
import io.lemonlabs.uri.config.UriConfig
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Try


package object utils {

  implicit class StringWithMacroDecorator(s: String) {

    private def encode(s: String) = java.net.URLEncoder.encode(s, "utf-8")

    def asMacro(escape: Boolean): String = {
      val m = "${" + s + "}"
      if (escape) encode(m) else m
    }

    def asMacro: String = asMacro(escape = false)

  }

  /**
    * For storing price in Druid as long => multiply x1000 and truncate fraction part of
    *
    * @param p price
    */
  implicit class PriceAsLong(p: Double) {

    def asLongX1K: Long = (p * 1000).toLong

  }

  implicit class UuidUtil(u: UUID) {

    val NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L

    def extractDateTime: Option[DateTime] =
      Try(
        new DateTime((u.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000).withZone(DateTimeZone.UTC)
      ).toOption.filter(_ != null)

  }

  implicit class StringCoder(s: String) {

    private val defaultCharset = StandardCharsets.UTF_8.name()

    def toUriEncoded: String = URLEncoder.encode(s, defaultCharset)

    def fromUriEncoded: String = URLDecoder.decode(s, defaultCharset)

    def toBase64: String = new String(Base64.getEncoder.encode(s.getBytes(defaultCharset)), defaultCharset)

    def fromBase64: String = new String(Base64.getDecoder.decode(s.getBytes(defaultCharset)))

    /**
      * Safe uri encode base64 string that encoded as uri segment
      *
      */
    def toUriBase64Encoded: String = s.toBase64.toUriEncoded


    /**
      * Safe decode base64 string that encoded as uri segment
      *
      */
    def fromUriBase64Encoded: String = s.fromUriEncoded.fromBase64

  }

  implicit class NotNullOption[T](val t: Try[T]) extends AnyVal {
    def toNotNullOption = t.toOption.flatMap {
      Option(_)
    }
  }

  implicit class SdkVersionParser(sdkVersionString: String) {

    import scala.util.Try


    def isNewSdkVersion: Boolean = {
      if (sdkVersionString.nonEmpty) {
        val ar = sdkVersionString.split("\\.").map(str => Try(str.toInt).toOption.getOrElse(-1))
        val a = ar.headOption.getOrElse(-1)
        if (a >= 2) true else false
      } else false
    }

  }

  implicit class PriceRounder(price: Double) {
    def roundPrice(digits: Int): Double = (math rint price * math.pow(10, digits)) / math.pow(10, digits)
  }

  implicit class UriConversions(string: String) {

    def toUri: Uri = Uri(string)

    def toLemonUri(implicit uriConfig: UriConfig): LemonUri = LemonUri.parse(string)
  }

  implicit class ProtoCreativeAttributeOps(attr: adcom.CreativeAttribute) {
    def convertToRtb = CreativeAttribute.withValueOpt(attr.value)
  }

}