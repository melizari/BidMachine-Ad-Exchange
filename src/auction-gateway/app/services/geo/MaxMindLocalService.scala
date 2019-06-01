package services.geo
import java.net.InetAddress

import com.appodealx.exchange.common.utils.CountryParser
import com.appodealx.openrtb.IpLocationService
import com.snowplowanalytics.maxmind.iplookups.IpLookups
import com.snowplowanalytics.maxmind.iplookups.model.IpLocation
import models.geo.GeoIpData
import monix.eval.Task
import play.api.Logger
import services.settings.MaxMindLocalSettings

import scala.language.postfixOps
import scala.util.control.NonFatal

class MaxMindLocalService(settings: MaxMindLocalSettings) extends GeoIpService[Task] {

  import cats.syntax.applicativeError._
  import cats.syntax.option._

  private val logger = Logger(getClass)

  private val lookupF = IpLookups
    .createFromFilenames[Task](
      geoFile = Some(settings.geoFile),
      lruCacheSize = settings.lruCacheSize
    )
    .memoize

  override def getGeoIpData(ip: InetAddress) = {
    for {
      lookup <- lookupF
      res    <- lookup.performLookups(ip.getHostAddress)
      locOpt = res.ipLocation.flatMap(_.toOption).flatMap(createGeoIpDataFromLocation)
    } yield locOpt
  } recover {
    case NonFatal(e) =>
      logger.error(s"${getClass.getName} responded with an error: $e")
      none[GeoIpData]
  }

  private def createGeoIpDataFromLocation(l: IpLocation) =
    for {
      countryCode <- CountryParser.parseCountryInstance(l.countryCode)
    } yield
      GeoIpData(
        country = countryCode,
        region = l.region,
        city = l.city,
        zip = l.postalCode,
        lat = l.latitude.toDouble.some,
        lon = l.longitude.toDouble.some,
        ipLocationService = IpLocationService.MaxMind
      )
}
