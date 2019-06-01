package com.appodealx.exchange.common.models.circe

import com.appodealx.exchange.common.models._
import com.appodealx.exchange.common.models.auction._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait CirceModelsInstances
  extends CirceMappedToInstances
    with CirceEnumInstances
    with CirceRtbInstances
    with CirceDtoInstances
    with CirceDateTimeInstances
    with CirceUriInstances
    with CirceVersionInstances {

  implicit val publisherIdDecoder = mappedToDecoder(PublisherId.apply)
  implicit val publisherDecoder = deriveDecoder[Publisher]
  implicit val publisherEncoder = deriveEncoder[Publisher]

  implicit val appIdDecoder = mappedToDecoder(AppId.apply)
  implicit val appDecoder = deriveDecoder[App]
  implicit val appEncoder = deriveEncoder[App]

  implicit val countryDecoder = mappedToDecoder(Country.apply)

  implicit val agencyIdDecoder = mappedToDecoder(AgencyId.apply)
  implicit val extAgencyIdDecoder = mappedToDecoder(AgencyExternalId.apply)
  implicit val agencyDecoder = deriveDecoder[Agency]
  implicit val agencyEncoder = deriveEncoder[Agency]

  implicit val bidderIdDecoder = mappedToDecoder(BidderId.apply)
  implicit val bidderDecoder = deriveDecoder[Bidder]
  implicit val bidderEncoder = deriveEncoder[Bidder]

  implicit val adProfileIdDecoder = mappedToDecoder(AdProfileId.apply)

  implicit val globalSettingsObjEncoder = deriveEncoder[GlobalConfig]
  implicit val globalSettingsObjDecoder = deriveDecoder[GlobalConfig]

}
