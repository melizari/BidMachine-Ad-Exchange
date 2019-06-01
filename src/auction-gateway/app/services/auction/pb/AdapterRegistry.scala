package services.auction.pb

trait AdapterRegistry[F[_]] extends (String => Option[Adapter[F]])
