package services.auction.pb.adapters.pubnative.model

case class MetaData(link: Option[String] = None,
                    icon: Option[String] = None,
                    text: Option[String] = None,
                    number: Option[Int] = None)