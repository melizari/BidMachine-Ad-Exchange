package com.appodealx.exchange.common.utils

import com.appodealx.openrtb
import com.appodealx.openrtb.Imp


package object analytics {

  def priceLevel(value: Double) = {
    def format(d: Double) = "%.2f".format(d)

    value match {
      case v@0 => v.toString
      case _ =>
        val r = math.floor(value * 10)
        s"${format(r / 10)}-${format((r + 1) / 10)}"
    }
  }

  /**
    * Get ad size as sting like "300x200"
    *
    * @param imp optional impression
    * @return String representation of impression ad size
    */
  def impressionAdSizeString(imp: Imp): String =
    imp match {
      case impressionBanner if impressionBanner.banner.isDefined =>
        val w = impressionBanner.banner.get.w.getOrElse(0)
        val h = impressionBanner.banner.get.h.getOrElse(0)
        w + "x" + h
      case impressionVideo if impressionVideo.video.isDefined =>
        val w = impressionVideo.video.get.w.getOrElse(0)
        val h = impressionVideo.video.get.h.getOrElse(0)
        w + "x" + h
      case _ => "unknown"
    }

  /**
    * Get ad size as tuple like (width, height)
    *
    * @param impOpt optional impression
    * @return Tuple representation of impression ad size
    */
  def impressionAdSizeTuple(impOpt: Option[Imp]): Option[(Long, Long)] =
    impOpt.flatMap {
      case imp if imp.banner.isDefined =>
        val w = imp.banner.get.w.getOrElse(0)
        val h = imp.banner.get.h.getOrElse(0)
        Some((w, h))
      case imp if imp.video.isDefined =>
        val w = imp.video.get.w.getOrElse(0)
        val h = imp.video.get.h.getOrElse(0)
        Some(w, h)
      case _ => None
    }

  /**
    * Convert IAB category string to IAB with decoded string
    *
    * @param cat string
    * @return string
    */
  def convertCategory(cat: String): String = cat +
    (if (IABCategoriesDecoder.exist(cat)) {
      " (" + IABCategoriesDecoder.decode(cat) + ")"
    } else {
      ""
    })

  def parseBcat(ext: openrtb.Json): Option[List[String]] =
    ext.hcursor.get[List[String]]("bcat").toOption

  def parseBadv(ext: openrtb.Json): Option[List[String]] =
    ext.hcursor.get[List[String]]("badv").toOption

}