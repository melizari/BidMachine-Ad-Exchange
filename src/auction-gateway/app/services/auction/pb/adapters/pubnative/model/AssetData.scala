package services.auction.pb.adapters.pubnative.model

case class AssetData(w: Option[Int] = None,
                     h: Option[Int] = None,
                     url: Option[String] = None,
                     text: Option[String] = None,
                     number: Option[Int] = None,
                     html: Option[String] = None,
                     vast2: Option[String] = None)
