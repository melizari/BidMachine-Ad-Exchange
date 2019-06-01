package services.auction.pb.adapters.criteo.s2s.model.rq

import io.circe.Encoder
import io.circe.derivation.deriveEncoder

private[s2s] case class User(id: Option[String],
                buyeruid: Option[String] = None,
                country: Option[String],
                language: Option[String] = None,
                useragent: Option[String] = None,
                dnt: Option[Int] = None,
                coppa: Option[Boolean],
                geo: Option[Geo],
                yob: Option[Int],
                gdpr: Option[Boolean],
                consent: Option[String])

private[s2s] object User {
  private implicit val booleanEncoder = Encoder.encodeInt.contramap[Boolean](if (_) 1 else 0)

  implicit val encoder = deriveEncoder[User]
}
