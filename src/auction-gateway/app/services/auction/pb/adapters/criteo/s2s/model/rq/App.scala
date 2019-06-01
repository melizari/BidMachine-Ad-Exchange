package services.auction.pb.adapters.criteo.s2s.model.rq
import io.circe.derivation.deriveEncoder

private[s2s] case class App(id: Option[String],
               name: Option[String],
               bundle: String,
               domain: Option[String],
               storeurl: Option[String])
private[s2s] object App {
  implicit val encoder = deriveEncoder[App]
}
