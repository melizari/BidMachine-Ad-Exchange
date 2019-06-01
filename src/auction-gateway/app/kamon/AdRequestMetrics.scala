package kamon

import com.appodealx.exchange.common.models.auction.Plc
import com.appodealx.exchange.common.models.dto.{Banner, Native, Video}
import com.appodealx.exchange.common.utils.CountryParser
import models.auction.AdRequest

import cats.ApplicativeError

object AdRequestMetrics {

  import CountryParser._

  import cats.syntax.applicativeError._

  private val adRequestCounter = Kamon.counter("ad_request")

  val BANNER       = "banner"
  val INTERSTITIAL = "banner_instl"
  val VIDEO        = "video"
  val NATIVE       = "native"

  private def adType[T: Plc](instl: Boolean) = Plc[T] match {
    case placement if placement.is[Banner] && instl => AdRequestMetrics.INTERSTITIAL
    case placement if placement.is[Banner]          => AdRequestMetrics.BANNER
    case placement if placement.is[Video]           => AdRequestMetrics.VIDEO
    case placement if placement.is[Native]          => AdRequestMetrics.NATIVE
  }

  def measureMetrics[F[_], A: Plc](
    country: Option[String],
    platform: Option[String],
    adSpaceId: Option[Long],
    sellerId: Option[Long],
    interstitial: Boolean,
    pb: Boolean
  )(implicit A: ApplicativeError[F, Throwable]): F[Unit] =
    A.pure {

      val validatedCountry = country.flatMap(parseAlpha3).getOrElse("ZZZ")

      val tags = List(
        Some("ad_type"              -> adType(interstitial)),
        Some("country"              -> validatedCountry),
        platform.map("platform"     -> _),
        adSpaceId.map("ad_space_id" -> _.toString),
        sellerId.map("seller_id"    -> _.toString),
        Some("pb"                   -> pb.toString)
      ).flatten

      adRequestCounter.refine(tags: _*).increment()
    }.handleError(_ => ())

  def measureMetrics[F[_], P: Plc](req: AdRequest[P], pb: Boolean)(
    implicit A: ApplicativeError[F, Throwable]
  ): F[Unit] =
    measureMetrics(
      country = req.device.geo.flatMap(_.country),
      platform = req.device.os,
      adSpaceId = req.adSpaceId.map(_.value),
      sellerId = req.sellerId,
      interstitial = req.interstitial,
      pb = pb
    )
}
