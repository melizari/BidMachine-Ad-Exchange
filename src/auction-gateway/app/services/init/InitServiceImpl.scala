package services.init

import com.neovisionaries.i18n.CountryCode
import io.bidmachine.protobuf.InitResponse
import models.geo.GeoIpData
import play.api.Logger
import services.geo.{GeoIpServiceProxy, GeoUtils}
import services.settings.DataCenterEndpointsSettings

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.functor._
import cats.syntax.option._

class InitServiceImpl[F[_]: Applicative](geoIpProxy: GeoIpServiceProxy[F], endpoints: DataCenterEndpointsSettings)
    extends InitService[F] {

  override def init(ip: String): F[Option[InitResponse]] = {
    decodeIp(ip).map {
      case CountryCode.US if endpoints.us.nonEmpty =>
        Logger.debug(s"Get country code: US")
        Logger.debug(s"Returning head of ${endpoints.us}")
        endpoints.us.headOption

      case code =>
        Logger.debug(s"Get country code: $code")
        Logger.debug(s"Returning head of ${endpoints.eu}")
        endpoints.eu.headOption
    }.map(_.map(InitResponse(_)))
  }

  private def decodeIp(ip: String) = {

    val address = GeoUtils tryGetInetAddress ip

    val geoData = address match {
      case Some(a) => geoIpProxy.getGeoIpData(a)
      case _       => none[GeoIpData].pure[F]
    }

    geoData map {
      case Some(geo) => geo.country
      case None      => CountryCode.EU
    }
  }
}
