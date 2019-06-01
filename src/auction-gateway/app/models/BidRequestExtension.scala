package models


case class BidRequestExtension(`ssp_id`: Long,
                               `ad_space_id`: Long,
                               `sdk_version`: Option[String] = None,
                               `sdk_name`: Option[String] = None,
                               `metadata_headers`: Option[Int] = None)
