package com.appodealx.exchange.common.models.rtb.vast

import com.appodealx.exchange.common.utils.xtract.XmlReader._
import com.appodealx.exchange.common.utils.xtract.XmlWriter.syntax._
import com.appodealx.exchange.common.utils.xtract._
import play.api.libs.functional.syntax._

case class MediaFiles(mediaFiles: List[MediaFile],
                      mezzanines: List[Mezzanine],
                      interactiveCreativeFiles: List[InteractiveCreativeFile])

object  MediaFiles {

  implicit val xmlReader = (
    (__ \ "MediaFile").read(strictReadSeq[MediaFile]) ~
    (__ \ "Mezzanine").read(strictReadSeq[Mezzanine]) ~
    (__ \ "InteractiveCreativeFile").read(strictReadSeq[InteractiveCreativeFile]))(MediaFiles.apply _)

  implicit val xmlWriter = XmlWriter { v: MediaFiles =>
    <MediaFiles>
      {v.mediaFiles.toXml}
      {v.mezzanines.toXml}
      {v.interactiveCreativeFiles.toXml}
    </MediaFiles>
  }

}