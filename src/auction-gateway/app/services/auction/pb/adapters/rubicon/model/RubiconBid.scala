package services.auction.pb.adapters.rubicon.model

import com.appodealx.openrtb.Json

case class RubiconBid(id: String,
                      impid: String,
                      price: Double,
                      estimated: Option[String] = None,
                      dealid: Option[String] = None,
                      pmptier: Option[Int] = None,
                      adm: Option[String] = None,
                      adomain: Option[List[String]] = None,
                      admobject: Option[Json] = None,
                      nurl: Option[String] = None,
                      crid: Option[String] = None,
                      w: Option[Int] = None,
                      h: Option[Int] = None,
                      ext: Option[Json] = None)
