package services.geo

import java.net.InetAddress

import cats.effect.{ Concurrent, Timer }
import models.geo.GeoIpData
import play.api.Logger
import services.settings.GeoIpSettings

class GeoIpServiceProxyImp[F[_]: Concurrent: Timer](geoIpServices: List[GeoIpService[F]], geoIpSettings: GeoIpSettings)
    extends GeoIpServiceProxy[F] {

  import cats.effect.syntax.concurrent._
  import cats.syntax.functor._

  type Service  = GeoIpService[F]
  type Response = Option[GeoIpData]

  private val logger = Logger(getClass)

  override def getGeoIpData(ip: InetAddress): F[Response] =
    Concurrent[F]
      .tailRecM[List[Service], Response](geoIpServices) {
        case h :: t =>
          h.getGeoIpData(ip).map {
            case None    => Left(t)
            case geoData => Right(geoData)
          }
        case Nil =>
          logger.warn(s"Could not find GeoIpData. Services which were asked: ${geoIpServices.map(_.getClass.getName)}")
          Concurrent[F].pure(Right(None))
      }
      .timeout(geoIpSettings.ttl)
}
