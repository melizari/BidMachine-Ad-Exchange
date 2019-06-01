package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class MediaFile(delivery: Delivery,
                     `type`: String,
                     width: Int,
                     height: Int,
                     codec: Option[String] = None,
                     id: Option[String] = None,
                     bitrate: Option[Int] = None,
                     minBitrate: Option[Int] = None,
                     maxBitrate: Option[Int] = None,
                     scalable: Option[Boolean] = None,
                     maintainAspectRatio: Option[Boolean] = None,
                     apiFramework: Option[String] = None,
                     value: Uri)

object MediaFile {

  implicit val xmlReader = (
    attribute[Delivery]("delivery") ~
    attribute[String]("type") ~
    attribute[Int]("width") ~
    attribute[Int]("height") ~
    attribute[String]("codec").optional ~
    attribute[String]("id").optional ~
    attribute[Int]("bitrate").optional ~
    attribute[Int]("minBitrate").optional ~
    attribute[Int]("maxBitrate").optional ~
    attribute[Boolean]("scalable").optional ~
    attribute[Boolean]("maintainAspectRatio").optional ~
    attribute[String]("apiFramework").optional ~
    __.read[Uri])(MediaFile.apply _)

  implicit val xmlWriter = XmlWriter { v: MediaFile =>

    val attrs =
      Nil ++
      v.codec.map("codec" -> _) ++
      v.id.map("id" -> _) ++
      v.bitrate.map("bitrate" -> _.toString)
      v.minBitrate.map("minBitrate" -> _.toString) ++
      v.maxBitrate.map("maxBitrate" -> _.toString) ++
      v.scalable.map("scalable" -> _.toString) ++
      v.maintainAspectRatio.map("maintainAspectRatio" -> _.toString) ++
      v.apiFramework.map("apiFramework" -> _)

    <MediaFile delivery={v.delivery.entryName} type={v.`type`} width={v.width.toString} height={v.height.toString}>
      {xml.PCData(v.value.toString)}
    </MediaFile> addAttrs attrs
  }

}

