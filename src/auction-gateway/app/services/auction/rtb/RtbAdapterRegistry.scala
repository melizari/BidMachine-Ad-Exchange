package services.auction.rtb

trait RtbAdapterRegistry[F[_]] extends (String => Option[Adapter[F]]){

}
