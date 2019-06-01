package models.auction

import services.auction.Headers._

case class Metadata(
  renderMetadata: Boolean,
  `X-Appodeal-Adomain`: Option[String] = None,
  `X-Appodeal-Bid-Request-ID`: String,
  `X-Appodeal-Cache`: Boolean,
  `X-Appodeal-Campaign-ID`: Option[String] = None,
  `X-Appodeal-Close-Time`: Int,
  `X-Appodeal-Creative-ID`: Option[String] = None,
  `X-Appodeal-Demand-Source`: Option[String] = None,
  `X-Appodeal-Displaymanager`: Option[String] = None,
  `X-Appodeal-Identifier`: Option[String] = None,
  `X-Appodeal-Impression-ID`: Option[String] = None,
  `X-Appodeal-Price`: Double,
  `X-Appodeal-Price-Type`: String = "CPM",
  `X-Appodeal-Price-Currency`: String = "USD",
  `X-Appodeal-Url-Click`: Option[String] = None,
  `X-Appodeal-Url-Impression`: Option[String] = None,
  `X-Appodeal-Url-Finish`: Option[String] = None,
  `X-Appodeal-Url-Fill`: Option[String] = None,
  `X-Appodeal-Url-Error`: Option[String] = None,
  `X-Appodeal-Ad-Type`: String
)

object Metadata {

  implicit class MetadataOps(m: Metadata) {

    def toList =
      List(
        `X-Appodeal-Price`          -> m.`X-Appodeal-Price`.toString,
        `X-Appodeal-Ad-Type`        -> m.`X-Appodeal-Ad-Type`,
        `X-Appodeal-Cache`          -> m.`X-Appodeal-Cache`.toString,
        `X-Appodeal-Close-Time`     -> m.`X-Appodeal-Close-Time`.toString,
        `X-Appodeal-Price-Type`     -> m.`X-Appodeal-Price-Type`,
        `X-Appodeal-Price-Currency` -> m.`X-Appodeal-Price-Currency`,
        `X-Appodeal-Bid-Request-ID` -> m.`X-Appodeal-Bid-Request-ID`
      ) ++
        m.`X-Appodeal-Adomain`.map(`X-Appodeal-Adomain`               -> _) ++
        m.`X-Appodeal-Campaign-ID`.map(`X-Appodeal-Campaign-ID`       -> _) ++
        m.`X-Appodeal-Creative-ID`.map(`X-Appodeal-Creative-ID`       -> _) ++
        m.`X-Appodeal-Demand-Source`.map(`X-Appodeal-Demand-Source`   -> _) ++
        m.`X-Appodeal-Displaymanager`.map(`X-Appodeal-Displaymanager` -> _) ++
        m.`X-Appodeal-Identifier`.map(`X-Appodeal-Identifier`         -> _) ++
        m.`X-Appodeal-Impression-ID`.map(`X-Appodeal-Impression-ID`   -> _) ++
        m.`X-Appodeal-Url-Click`.map(`X-Appodeal-Url-Click`           -> _) ++
        m.`X-Appodeal-Url-Impression`.map(`X-Appodeal-Url-Impression` -> _) ++
        m.`X-Appodeal-Url-Fill`.map(`X-Appodeal-Url-Fill`             -> _) ++
        m.`X-Appodeal-Url-Finish`.map(`X-Appodeal-Url-Finish`         -> _) ++
        m.`X-Appodeal-Url-Error`.map(`X-Appodeal-Url-Error`           -> _)
  }
}
