package com.appodealx.exchange.common.models.auction

import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.dto.{Banner, Native, Video}
import com.appodealx.exchange.common.utils._
import com.appodealx.openrtb.{ApiFramework, CreativeAttribute, Imp}

import cats.instances.option._
import cats.syntax.contravariantSemigroupal._

trait Plc[P] extends TypeTag[P] { self =>

  def name: String
  def apiFramework: String

  def size(ad: P): Option[Size]

  def apiFrameworks(ad: P): Option[List[ApiFramework]]

  def battr(ad: P): Option[List[CreativeAttribute]]

  def is[P2: Plc] = Plc[P2] == self

  def toRtb2(repr: P, imp: Imp): Imp
}

object Plc {

  def apply[P: Plc] = implicitly[Plc[P]]

  implicit object BannerPlc extends Plc[Banner] {

    val name         = "banner"
    val apiFramework = "mraid"

    def size(ad: Banner) = (ad.w, ad.h).mapN(Size.apply)

    def apiFrameworks(ad: Banner): Option[List[ApiFramework]] = ad.api.map(_.toList)

    def battr(ad: Banner) = ad.battr.map(_.toList)

    def toRtb2(repr: Banner, imp: Imp): Imp = imp.copy(banner = Some(repr.toRtb))
  }

  implicit object VideoPlc extends Plc[Video] {

    val name         = "video"
    val apiFramework = "vast"

    def size(ad: Video) = (ad.w, ad.h).mapN(Size.apply)

    def apiFrameworks(ad: Video): Option[List[ApiFramework]] = ad.api.map(_.toList)

    def battr(ad: Video) = ad.battr.map(_.toList)

    def toRtb2(repr: Video, imp: Imp): Imp = imp.copy(video = Some(repr.toRtb))
  }

  implicit object NativePlc extends Plc[Native] {

    val name         = "native"
    val apiFramework = "nast"

    def size(ad: Native) = None

    def apiFrameworks(ad: Native): Option[List[ApiFramework]] = ad.api.map(_.toList)

    def battr(ad: Native) = ad.battr.map(_.toList)

    def toRtb2(repr: Native, imp: Imp): Imp = imp.copy(native = Some(repr.toRtb))
  }
}
