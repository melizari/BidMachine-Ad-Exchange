package models.geo

import com.appodealx.openrtb.IpLocationService
import com.neovisionaries.i18n.CountryCode


case class GeoIpData(country: CountryCode,
                     region: Option[String] = None,
                     city: Option[String] = None,
                     zip: Option[String] = None,
                     lat: Option[Double] = None,
                     lon: Option[Double] = None,
                     ipLocationService: IpLocationService)
